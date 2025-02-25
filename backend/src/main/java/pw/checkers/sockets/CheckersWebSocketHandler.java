package pw.checkers.sockets;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pw.checkers.data.GameState;
import pw.checkers.data.enums.Color;
import pw.checkers.data.enums.MessageType;
import pw.checkers.message.*;
import pw.checkers.game.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.checkers.utils.WaitingPlayer;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static pw.checkers.data.enums.MessageType.*;

public class CheckersWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(CheckersWebSocketHandler.class);

    private final Map<MessageType, WebSocketMessageHandler> handlers = new ConcurrentHashMap<>();
    private final GameService gameService;
    private final SessionManager sessionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Set<WebSocketSession>> rematchRequests = new ConcurrentHashMap<>();

    public CheckersWebSocketHandler(GameService gameService, SessionManager sessionManager) {
        this.gameService = gameService;
        this.sessionManager = sessionManager;
        initializeHandlers();
    }

    private void initializeHandlers() {
        handlers.put(MessageType.JOIN_QUEUE, (session, rawMessage) -> {
            QueueMessage queueMsg = convertContent(rawMessage, QueueMessage.class);
            handleJoinQueue(session, queueMsg.getUser());
        });

        handlers.put(MessageType.LEAVE_QUEUE, (session, rawMessage) -> {
            QueueMessage queueMsg = convertContent(rawMessage, QueueMessage.class);
            handleLeaveQueue(session, queueMsg.getUser());
        });

        handlers.put(MessageType.MOVE, (session, rawMessage) -> {
            MoveInput moveInput = convertContent(rawMessage, MoveInput.class);
            handleMove(session, moveInput);
        });

        handlers.put(MessageType.POSSIBILITIES, (session, rawMessage) -> {
            PossibilitiesInput possibilitiesInput = convertContent(rawMessage, PossibilitiesInput.class);
            handlePossibilities(session, possibilitiesInput);
        });

        handlers.put(MessageType.REMATCH_REQUEST, (session, rawMessage) -> {
            GameIdMessage gameIdMessage = convertContent(rawMessage, GameIdMessage.class);
            proposeRematch(session, gameIdMessage);
        });

        handlers.put(MessageType.ACCEPT_REMATCH, (session, rawMessage) -> {
            GameIdMessage gameIdMessage = convertContent(rawMessage, GameIdMessage.class);
            startRematch(session, gameIdMessage);
        });

        handlers.put(MessageType.DECLINE_REMATCH, (session, rawMessage) -> {
            GameIdMessage gameIdMessage = convertContent(rawMessage, GameIdMessage.class);
            sendRejection(session, gameIdMessage);
            cleanGameHistory(gameIdMessage);
            sessionManager.removeUsersBySessionEntry(session);
        });

        handlers.put(MessageType.LEAVE, (session, rawMessage) -> {
            GameIdMessage gameIdMessage = convertContent(rawMessage, GameIdMessage.class);
            cleanGameHistory(gameIdMessage);
            sessionManager.removeUsersBySessionEntry(session);
        });
    }

    private <T> T convertContent(Message<Map<String, Object>> rawMessage, Class<T> tClass){
        return objectMapper.convertValue(rawMessage.getContent(), tClass);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.debug("Connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        logger.debug("Message received: {}", message.getPayload());

        Message<Map<String, Object>> rawMessage = objectMapper.readValue(
                message.getPayload(),
                new TypeReference<>() {}
        );
        MessageType type = MessageType.fromString(rawMessage.getType());
        WebSocketMessageHandler handler = handlers.get(type);
        if (handler != null) {
            handler.handle(session, rawMessage);
        } else {
            Message<String> defaultMessage = new Message<>(ERROR.getValue(), "Unknown message type: " + rawMessage.getType());
            sendMessage(session, defaultMessage);
        }
    }

    private void handleLeaveQueue(WebSocketSession session, User user) {
        sessionManager.removeWaitingPlayerFromQueue(session, user);
    }

    private void sendRejection(WebSocketSession session, GameIdMessage gameIdMessage) throws IOException {
        String gameId = gameIdMessage.getGameId();

        Optional<WebSocketSession> opponent = sessionManager.getOpponent(gameId, session);

        if (opponent.isPresent()) {
            Message<PromptMessage> message =
                    new Message<>(REJECTION.getValue(), new PromptMessage("Your opponent reject your rematch request"));
            sendMessage(opponent.get(), message);
        } else {
            Message<PromptMessage> message =
                    new Message<>(REJECTION.getValue(), new PromptMessage("Opponent has already left the game"));
            sendMessage(session, message);
        }

    }

    private void cleanGameHistory(GameIdMessage gameIdMessage) {
        String gameId = gameIdMessage.getGameId();
        sessionManager.removeGameFromMaps(gameId);
        gameService.deleteGame(gameId);
        rematchRequests.remove(gameId);
    }

    private void proposeRematch(WebSocketSession session, GameIdMessage gameIdMessage) throws IOException {
        String gameId = gameIdMessage.getGameId();
        Set<WebSocketSession> sessions = sessionManager.getSessionsByGameId(gameId);
        if (sessions == null) {
            Message<PromptMessage> message =
                    new Message<>(REJECTION.getValue(), new PromptMessage("Opponent has already left the game"));
            sendMessage(session, message);
            return;
        }
        Optional<WebSocketSession> opponent = sessionManager.getOpponent(gameId, session);

        rematchRequests.putIfAbsent(gameId, ConcurrentHashMap.newKeySet());
        Set<WebSocketSession> rematchSet = rematchRequests.get(gameId);

        synchronized (rematchSet) {
            rematchSet.add(session);
            if (rematchSet.size() == sessions.size()) {
                startRematch(session, gameIdMessage);
                rematchRequests.remove(gameId);
                return;
            }
        }

        if (opponent.isPresent()) {
            Message<GameIdMessage> message =
                    new Message<>(REMATCH_REQUEST.getValue(), new GameIdMessage(gameId));
            sendMessage(opponent.get(), message);
        } else {
            Message<PromptMessage> message =
                    new Message<>(REJECTION.getValue(), new PromptMessage("Opponent has already left the game"));
            sendMessage(session, message);
        }

    }

    private String createGame() {
        GameState newGame = gameService.createGame();
        return newGame.getGameId();
    }



    private void startRematch(WebSocketSession session, GameIdMessage gameIdMessage) throws IOException {
        String gameId = gameIdMessage.getGameId();
        Map<WebSocketSession, String> gamePlayers = sessionManager.getColorAssignments(gameId);
        if (gamePlayers == null) {
            Message<PromptMessage> message = new Message<>(REJECTION.getValue(), new PromptMessage("Opponent has already left the game"));
            sendMessage(session, message);
            return;
        }
        Map<String, WebSocketSession> playersByColor = sessionManager.getSessionByColorMap(gameId);
        cleanGameHistory(gameIdMessage);
        String newGameId = createGame();

        sessionManager.addToSessionsByGame(newGameId, playersByColor.get(Color.WHITE.getValue()), playersByColor.get(Color.BLACK.getValue()));
        sessionManager.addToColorAssignments(newGameId, playersByColor.get(Color.BLACK.getValue()), playersByColor.get(Color.WHITE.getValue()));

        // white player will play black in rematch
        Message<JoinMessage> messageForOriginalWhite = new Message<>(
                GAME_CREATED.getValue(),
                new JoinMessage(newGameId, Color.BLACK.getValue(), sessionManager.getUserBySession(playersByColor.get(Color.BLACK.getValue())))
        );
        Message<JoinMessage> messageForOriginalBlack = new Message<>(
                GAME_CREATED.getValue(),
                new JoinMessage(newGameId, Color.WHITE.getValue(), sessionManager.getUserBySession(playersByColor.get(Color.WHITE.getValue())))
        );
        sendMessage(playersByColor.get(Color.WHITE.getValue()), Color.BLACK.getValue(), messageForOriginalWhite);
        sendMessage(playersByColor.get(Color.BLACK.getValue()), Color.WHITE.getValue(), messageForOriginalBlack);
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
        Message<PromptMessage> waitingMessage =
                new Message<>(WAITING.getValue(), new PromptMessage("Waiting for an opponent..."));
        sendMessage(session, waitingMessage);
    }

    private void createAndAssignGame(WaitingPlayer waitingPlayer, WebSocketSession session, User user) throws IOException {
        WebSocketSession waitingSession = waitingPlayer.session();
        String newGameId = createGame();

        sessionManager.addToSessionsByGame(newGameId, waitingSession, session);
        sessionManager.addToColorAssignments(newGameId, waitingSession, session);
        sessionManager.addToUserBySessions(waitingSession, waitingPlayer.user(), session, user);

        Message<JoinMessage> waitingPlayerResponse = new Message<>(
                GAME_CREATED.getValue(),
                new JoinMessage(newGameId, Color.WHITE.getValue(), new User(user.getUsername()))
        );
        Message<JoinMessage> sessionPlayerResponse = new Message<>(
                GAME_CREATED.getValue(),
                new JoinMessage(newGameId, Color.BLACK.getValue(), new User(waitingPlayer.user().getUsername()))
        );

        sendMessage(waitingSession, Color.WHITE.getValue(), waitingPlayerResponse);
        sendMessage(session, Color.BLACK.getValue(), sessionPlayerResponse);
    }

    private Optional<String> getAssignedColor(String gameId, WebSocketSession session) throws IOException {
        if (gameId == null) {
            sendError(session, "No game id specified");
            return Optional.empty();
        }
        if (sessionManager.isGameIdInvalid(gameId)) {
            sendError(session, "Game with id " + gameId + " not found");
            return Optional.empty();
        }
        String assignedColor = sessionManager.getAssignedColorByGameIdAndSession(gameId, session);
        if (assignedColor == null) {
            sendError(session, "You do not belong to this game or no color assigned");
            return Optional.empty();
        }
        return Optional.of(assignedColor);
    }

    private void handleMove(WebSocketSession session, MoveInput moveInput) throws IOException {
        String gameId = moveInput.getGameId();
        Optional<String> maybeColor = getAssignedColor(gameId, session);
        if (maybeColor.isEmpty()) return;
        String assignedColor = maybeColor.get();
        MoveOutput moveOutput = gameService.makeMove(gameId, moveInput.getMove(), assignedColor);
        Message<MoveOutput> moveMessage = new Message<>(MOVE.getValue(), moveOutput);
        GameState updatedState = gameService.getGame(gameId);

        broadcastToGame(gameId, moveMessage);

        if (updatedState.isFinished()) {
            broadcastGameEnd(gameId, updatedState);
        }

        if (moveOutput != null && moveOutput.isHasMoreTakes()) {
            PossibleMoves moves = gameService.getPossibleMoves(
                    updatedState,
                    moveOutput.getMove().getToRow(),
                    moveOutput.getMove().getToCol()
            );
            Message<PossibleMoves> responseMessage = new Message<>(POSSIBILITIES.getValue(), moves);
            sendMessage(session, assignedColor, responseMessage);
        }
    }

    private void handlePossibilities(WebSocketSession session, PossibilitiesInput possibilitiesInput) throws IOException {
        String gameId = possibilitiesInput.getGameId();
        Optional<String> maybeColor = getAssignedColor(gameId, session);
        if (maybeColor.isEmpty()) return;
        String assignedColor = maybeColor.get();
        GameState currentState = gameService.getGame(gameId);
        PossibleMoves moves = gameService.getPossibleMoves(
                currentState,
                possibilitiesInput.getRow(),
                possibilitiesInput.getCol()
        );
        Message<PossibleMoves> responseMessage = new Message<>(POSSIBILITIES.getValue(), moves);
        sendMessage(session, assignedColor, responseMessage);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        sessionManager.handleSessionClose(session);
        logger.debug("Connection closed: {}", session.getId());
    }

    private void sendError(WebSocketSession session, String error) throws IOException {
        Message<String> errorMessage = new Message<>(ERROR.getValue(), error);
        sendMessage(session, errorMessage);
    }

    private void sendMessage(WebSocketSession session, Message<?> message) throws IOException {
        String messageJson = objectMapper.writeValueAsString(message);
        logger.debug("Message sent to session {}: {}", session.getId(), messageJson);
        session.sendMessage(new TextMessage(messageJson));
    }

    private void sendMessage(WebSocketSession session, String color, Message<?> message) throws IOException {
        String messageJson = objectMapper.writeValueAsString(message);
        logger.debug("Message sent to color {} (session {}): {}", color, session.getId(), messageJson);
        session.sendMessage(new TextMessage(messageJson));
    }

    private void broadcastToGame(String gameId, Message<?> message) throws IOException {
        Set<WebSocketSession> sessions = sessionManager.getSessionsByGameId(gameId);
        for (WebSocketSession ws : sessions) {
            if (ws.isOpen()) {
                String wsColor = sessionManager.getAssignedColorByGameIdAndSession(gameId, ws);
                sendMessage(ws, wsColor, message);
            }
        }
    }

    private void broadcastGameEnd(String gameId, GameState updatedState) throws IOException {
        Set<WebSocketSession> sessions = sessionManager.getSessionsByGameId(gameId);
        for (WebSocketSession ws : sessions) {
            if (ws.isOpen()) {
                String wsColor = sessionManager.getAssignedColorByGameIdAndSession(gameId, ws);
                Message<GameEnd> gameEndMsg;
                if (updatedState.getWinner() == null) {
                    gameEndMsg = new Message<>(GAME_END.getValue(), new GameEnd("draw"));
                } else {
                    gameEndMsg = new Message<>(GAME_END.getValue(), new GameEnd(updatedState.getWinner().toString().toLowerCase()));
                }
                sendMessage(ws, wsColor, gameEndMsg);
            }
        }
    }
}
