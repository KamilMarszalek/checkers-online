package pw.checkers.sockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.sun.jdi.InvalidTypeException;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pw.checkers.data.GameState;
import pw.checkers.data.enums.Color;
import pw.checkers.data.enums.MessageType;
import pw.checkers.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.checkers.utils.WaitingPlayer;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static pw.checkers.data.enums.MessageType.*;

public class CheckersWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(CheckersWebSocketHandler.class);

    private final Map<MessageType, WebSocketMessageHandler> handlers = new ConcurrentHashMap<>();
    private final SessionManager sessionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RematchService rematchService;
    private final MessageSender messageSender;
    private final GameManager gameManager;


    public CheckersWebSocketHandler(SessionManager sessionManager, RematchService rematchService, MessageSender messageSender, GameManager gameManager) {
        this.sessionManager = sessionManager;
        this.rematchService = rematchService;
        this.messageSender = messageSender;
        this.gameManager = gameManager;
        initializeHandlers();
    }

    private void initializeHandlers() {
        handlers.put(MessageType.JOIN_QUEUE, (session, rawMessage) -> {
            QueueMessage queueMsg = (QueueMessage) rawMessage;
            handleJoinQueue(session, queueMsg.getUser());
        });

        handlers.put(MessageType.LEAVE_QUEUE, (session, rawMessage) -> {
            QueueMessage queueMsg = (QueueMessage) rawMessage;
            handleLeaveQueue(session, queueMsg.getUser());
        });

        handlers.put(MessageType.MOVE, (session, rawMessage) -> {
            MoveInput moveInput = (MoveInput) rawMessage;
            handleMove(session, moveInput);
        });

        handlers.put(MessageType.POSSIBILITIES, (session, rawMessage) -> {
            PossibilitiesInput possibilitiesInput = (PossibilitiesInput) rawMessage;
            handlePossibilities(session, possibilitiesInput);
        });

        handlers.put(MessageType.REMATCH_REQUEST, (session, rawMessage) -> {
            GameIdMessage gameIdMessage = (GameIdMessage) rawMessage;
            proposeRematch(session, gameIdMessage);
        });

        handlers.put(MessageType.ACCEPT_REMATCH, (session, rawMessage) -> {
            GameIdMessage gameIdMessage = (GameIdMessage) rawMessage;
            startRematch(session, gameIdMessage);
        });

        handlers.put(MessageType.DECLINE_REMATCH, (session, rawMessage) -> {
            GameIdMessage gameIdMessage = (GameIdMessage) rawMessage;
            sendRejection(session, gameIdMessage);
            gameManager.cleanGameHistory(gameIdMessage);
            rematchService.removeFromRematchRequests(gameIdMessage.getGameId());
            sessionManager.removeUsersBySessionEntry(session);
        });

        handlers.put(MessageType.LEAVE, (session, rawMessage) -> {
            GameIdMessage gameIdMessage = (GameIdMessage) rawMessage;
            gameManager.cleanGameHistory(gameIdMessage);
            sessionManager.removeUsersBySessionEntry(session);
        });
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.debug("Connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.debug("Message received: {}", message.getPayload());
        Message rawMessage;
        try {
            rawMessage = objectMapper.readValue(message.getPayload(), Message.class);
        } catch (InvalidTypeIdException e) {
            Message defaultMessage = new PromptMessage(ERROR.getValue(), "Unknown message type");
            messageSender.sendMessage(session, defaultMessage);
            return;
        }
        MessageType type = MessageType.fromString(rawMessage.getType());
        WebSocketMessageHandler handler = handlers.get(type);
        handler.handle(session, rawMessage);
    }

    private void handleLeaveQueue(WebSocketSession session, User user) {
        sessionManager.removeWaitingPlayerFromQueue(session, user);
    }

    private void sendRejection(WebSocketSession session, GameIdMessage gameIdMessage) throws IOException {
        String gameId = gameIdMessage.getGameId();

        Optional<WebSocketSession> opponent = sessionManager.getOpponent(gameId, session);

        if (opponent.isPresent()) {
            Message message = new PromptMessage(REJECTION.getValue(), "Your opponent reject your rematch request");
            messageSender.sendMessage(opponent.get(), message);
        } else {
            Message message = new PromptMessage(REJECTION.getValue(), "Opponent has already left the game");
            messageSender.sendMessage(session, message);
        }

    }

    private void proposeRematch(WebSocketSession session, GameIdMessage gameIdMessage) throws IOException {
        rematchService.proposeRematch(session, gameIdMessage);
    }

    private void startRematch(WebSocketSession session, GameIdMessage gameIdMessage) throws IOException {
        rematchService.startRematch(session, gameIdMessage);
    }

    private void handleJoinQueue(WebSocketSession session, User user) throws IOException {
        WaitingPlayer waitingPlayer = sessionManager.pollFromPlayerQueue();

        if (waitingPlayer == null) {
            addPlayerToQueue(session, user);
        } else {
            createAndAssignGame(waitingPlayer, session, user);
        }
    }

    private void addPlayerToQueue(WebSocketSession session, User user) throws IOException {
        sessionManager.addPlayerToQueue(session, user);
        Message waitingMessage = new PromptMessage(WAITING.getValue(), "Waiting for an opponent...");
        messageSender.sendMessage(session, waitingMessage);
    }

    private void createAndAssignGame(WaitingPlayer waitingPlayer, WebSocketSession session, User user) throws IOException {
        WebSocketSession waitingSession = waitingPlayer.session();
        String newGameId = gameManager.createGame();

        sessionManager.addToSessionsByGame(newGameId, waitingSession, session);
        sessionManager.addToColorAssignments(newGameId, waitingSession, session);
        sessionManager.addToUserBySessions(waitingSession, waitingPlayer.user(), session, user);

        Message waitingPlayerResponse = new JoinMessage(newGameId, Color.WHITE.getValue(), new User(user.getUsername()));
        Message sessionPlayerResponse = new JoinMessage(newGameId, Color.BLACK.getValue(), new User(waitingPlayer.user().getUsername()));

        messageSender.sendMessage(waitingSession, Color.WHITE.getValue(), waitingPlayerResponse);
        messageSender.sendMessage(session, Color.BLACK.getValue(), sessionPlayerResponse);
    }

    private void handleMove(WebSocketSession session, MoveInput moveInput) throws IOException {
        String gameId = moveInput.getGameId();
        Optional<String> maybeColor = sessionManager.getAssignedColor(gameId, session);
        if (maybeColor.isEmpty()) return;
        String assignedColor = maybeColor.get();
        MoveOutput moveOutput = gameManager.makeMove(gameId, moveInput.getMove(), assignedColor);
        GameState updatedState = gameManager.getGame(gameId);

        messageSender.broadcastToGame(sessionManager.getSessionsByGameId(gameId), moveOutput, sessionManager.getColorAssignments(gameId));

        if (updatedState.isFinished()) {
            messageSender.broadcastGameEnd(sessionManager.getSessionsByGameId(gameId), updatedState, sessionManager.getColorAssignments(gameId));
        }

        if (moveOutput != null && moveOutput.isHasMoreTakes()) {
            PossibilitiesOutput moves = gameManager.getPossibleMoves(
                    new PossibilitiesInput(gameId,
                            moveOutput.getMove().getToRow(),
                            moveOutput.getMove().getToCol()),
                    session
            );
            messageSender.sendMessage(session, assignedColor, moves);
        }
    }

    private void handlePossibilities(WebSocketSession session, PossibilitiesInput possibilitiesInput) throws IOException {
        PossibilitiesOutput moves = gameManager.getPossibleMoves(possibilitiesInput, session);
        if (moves == null) return;
        Optional<String> maybeColor = sessionManager.getAssignedColor(possibilitiesInput.getGameId(), session);
        if (maybeColor.isEmpty()) return;
        String assignedColor = maybeColor.get();
        messageSender.sendMessage(session, assignedColor, moves);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        sessionManager.handleSessionClose(session);
        logger.debug("Connection closed: {}", session.getId());
    }
}
