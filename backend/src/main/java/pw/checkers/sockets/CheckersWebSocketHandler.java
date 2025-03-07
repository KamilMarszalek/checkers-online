package pw.checkers.sockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pw.checkers.data.GameState;
import pw.checkers.data.enums.Color;
import pw.checkers.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.checkers.utils.WaitingPlayer;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static pw.checkers.data.enums.MessageType.*;

public class CheckersWebSocketHandler extends TextWebSocketHandler implements MessageVisitor {

    private static final Logger logger = LoggerFactory.getLogger(CheckersWebSocketHandler.class);

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
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        logger.debug("Connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.debug("Message received: {}", message.getPayload());
        MessageAccept rawMessage;
        try {
            rawMessage = (MessageAccept) objectMapper.readValue(message.getPayload(), Message.class);
        } catch (InvalidTypeIdException e) {
            Message defaultMessage = new PromptMessage(ERROR.getValue(), "Unknown message type");
            messageSender.sendMessage(session, defaultMessage);
            return;
        }
        rawMessage.accept(this, session);
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
            gameManager.setGameEndReason(gameId, false);
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

    private void handleAcceptRematch(WebSocketSession session, AcceptRematchMessage acceptRematchMessage) throws IOException {
        startRematch(session, acceptRematchMessage);
    }

    private void handleLeave(WebSocketSession session, LeaveMessage leaveMessage) throws IOException {
        gameManager.cleanGameHistory(leaveMessage);
        sessionManager.removeUsersBySessionEntry(session);
    }

    private void handleDeclineRematch(WebSocketSession session, DeclineRematchMessage declineRematchMessage) throws IOException {
        sendRejection(session, declineRematchMessage);
        gameManager.cleanGameHistory(declineRematchMessage);
        rematchService.removeFromRematchRequests(declineRematchMessage.getGameId());
        sessionManager.removeUsersBySessionEntry(session);
    }

    private void handleRematchRequest(WebSocketSession session, RematchRequestMessage rematchRequestMessage) throws IOException {
        proposeRematch(session, rematchRequestMessage);
    }

    private void handleResign(WebSocketSession session, ResignMessage resignMessage) throws IOException {
        Map<WebSocketSession, String> colorsBySession = sessionManager.getColorAssignments(resignMessage.getGameId());
        String assignedColor = sessionManager.getAssignedColorByGameIdAndSession(resignMessage.getGameId(), session);
        String opponentColor = assignedColor.equals("white") ? "black" : "white";
        Set<WebSocketSession> sessions = sessionManager.getSessionsByGameId(resignMessage.getGameId());
        GameState updatedState = gameManager.setGameEnd(resignMessage, opponentColor);
        messageSender.broadcastGameEnd(sessions, updatedState, colorsBySession);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        sessionManager.handleSessionClose(session);
        logger.debug("Connection closed: {}", session.getId());
    }

    @Override
    public void visit(JoinQueueMessage message, WebSocketSession session) throws IOException {
        handleJoinQueue(session, message.getUser());
    }

    @Override
    public void visit(LeaveQueueMessage message, WebSocketSession session) {
        handleLeaveQueue(session, message.getUser());
    }

    @Override
    public void visit(MoveInput message, WebSocketSession session) throws IOException {
        handleMove(session, message);
    }

    @Override
    public void visit(PossibilitiesInput message, WebSocketSession session) throws IOException {
        handlePossibilities(session, message);
    }

    @Override
    public void visit(AcceptRematchMessage message, WebSocketSession session) throws IOException {
        handleAcceptRematch(session, message);
    }

    @Override
    public void visit(LeaveMessage message, WebSocketSession session) throws IOException {
        handleLeave(session, message);
    }

    @Override
    public void visit(DeclineRematchMessage message, WebSocketSession session) throws IOException {
        handleDeclineRematch(session, message);
    }

    @Override
    public void visit(RematchRequestMessage message, WebSocketSession session) throws IOException {
        handleRematchRequest(session, message);
    }

    @Override
    public void visit(ResignMessage message, WebSocketSession session) throws IOException {
        handleResign(session, message);
    }
}
