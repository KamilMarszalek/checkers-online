package pw.checkers.sockets;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pw.checkers.data.GameState;
import pw.checkers.data.enums.MessageType;
import pw.checkers.message.*;
import pw.checkers.game.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.checkers.utils.WaitingPlayer;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class CheckersWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(CheckersWebSocketHandler.class);

    private final Map<MessageType, WebSocketMessageHandler> handlers = new ConcurrentHashMap<>();
    private final GameService gameService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Set<WebSocketSession>> sessionsByGame = new ConcurrentHashMap<>();
    private final Map<String, Map<WebSocketSession, String>> colorAssignmentsByGame = new ConcurrentHashMap<>();
    private final Map<WebSocketSession, User> usersBySessions = new ConcurrentHashMap<>();
    private final Queue<WaitingPlayer> waitingQueue = new ConcurrentLinkedQueue<>();
    private final Map<String, Set<WebSocketSession>> rematchRequests = new ConcurrentHashMap<>();

    public CheckersWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
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
            usersBySessions.remove(session);
        });

        handlers.put(MessageType.LEAVE, (session, rawMessage) -> {
            GameIdMessage gameIdMessage = convertContent(rawMessage, GameIdMessage.class);
            cleanGameHistory(gameIdMessage);
            usersBySessions.remove(session);
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
            Message<String> defaultMessage = new Message<>("error", "Unknown message type: " + rawMessage.getType());
            sendMessage(session, defaultMessage);
        }
    }

    private void handleLeaveQueue(WebSocketSession session, User user) {
        waitingQueue.removeIf(waitingPlayer -> waitingPlayer.session().equals(session) && waitingPlayer.user().equals(user));
    }

    private Optional<WebSocketSession> getOpponent(String gameId, WebSocketSession session) throws IOException {
        Set<WebSocketSession> sessions = sessionsByGame.get(gameId);
        if (sessions == null || sessions.isEmpty()) {
            return Optional.empty();
        }
        return sessions.stream()
                .filter(s -> !s.equals(session))
                .findFirst();
    }

    private void sendRejection(WebSocketSession session, GameIdMessage gameIdMessage) throws IOException {
        String gameId = gameIdMessage.getGameId();

        Optional<WebSocketSession> opponent = getOpponent(gameId, session);

        if (opponent.isPresent()) {
            Message<PromptMessage> message =
                    new Message<>("rejection", new PromptMessage("Your opponent reject your rematch request"));
            sendMessage(opponent.get(), message);
        } else {
            Message<PromptMessage> message =
                    new Message<>("rejection", new PromptMessage("Opponent has already left the game"));
            sendMessage(session, message);
        }

    }

    private void cleanGameHistory(GameIdMessage gameIdMessage) {
        String gameId = gameIdMessage.getGameId();
        colorAssignmentsByGame.remove(gameId);
        sessionsByGame.remove(gameId);
        gameService.deleteGame(gameId);
        rematchRequests.remove(gameId);
    }

    private void proposeRematch(WebSocketSession session, GameIdMessage gameIdMessage) throws IOException {
        String gameId = gameIdMessage.getGameId();
        Set<WebSocketSession> sessions = sessionsByGame.get(gameId);
        if (sessions == null) {
            Message<PromptMessage> message =
                    new Message<>("rejection", new PromptMessage("Opponent has already left the game"));
            sendMessage(session, message);
            return;
        }
        Optional<WebSocketSession> opponent = getOpponent(gameId, session);

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
                    new Message<>("rematch request", new GameIdMessage(gameId));
            sendMessage(opponent.get(), message);
        } else {
            Message<PromptMessage> message =
                    new Message<>("rejection", new PromptMessage("Opponent has already left the game"));
            sendMessage(session, message);
        }

    }

    private String createGame() {
        GameState newGame = gameService.createGame();
        return newGame.getGameId();
    }

    private void addToSessionsByGame (String newGameId, WebSocketSession session1, WebSocketSession session2) {
        sessionsByGame.putIfAbsent(newGameId, ConcurrentHashMap.newKeySet());
        sessionsByGame.get(newGameId).add(session1);
        sessionsByGame.get(newGameId).add(session2);
    }

    private void addToColorAssignments (String newGameId, WebSocketSession white, WebSocketSession black) {
        colorAssignmentsByGame.putIfAbsent(newGameId, new ConcurrentHashMap<>());
        colorAssignmentsByGame.get(newGameId).put(white, "black");
        colorAssignmentsByGame.get(newGameId).put(black, "white");
    }

    private void addToUserBySessions(WebSocketSession session1, User user1, WebSocketSession session2, User user2) {
        usersBySessions.put(session1, user1);
        usersBySessions.put(session2, user2);
    }

    private void startRematch(WebSocketSession session, GameIdMessage gameIdMessage) throws IOException {
        String gameId = gameIdMessage.getGameId();
        Map<WebSocketSession, String> gamePlayers = colorAssignmentsByGame.get(gameId);
        if (gamePlayers == null) {
            Message<PromptMessage> message = new Message<>("rejection", new PromptMessage("Opponent has already left the game"));
            sendMessage(session, message);
            return;
        }
        Map<String, WebSocketSession> playersByColor = gamePlayers.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        cleanGameHistory(gameIdMessage);
        String newGameId = createGame();

        addToSessionsByGame(newGameId, playersByColor.get("white"), playersByColor.get("black"));
        addToColorAssignments(newGameId, playersByColor.get("black"), playersByColor.get("white"));

        // white player will play black in rematch
        Message<JoinMessage> messageForOriginalWhite = new Message<>(
                "Game created",
                new JoinMessage(newGameId, "black", usersBySessions.get(playersByColor.get("black")))
        );
        Message<JoinMessage> messageForOriginalBlack = new Message<>(
                "Game created",
                new JoinMessage(newGameId, "white", usersBySessions.get(playersByColor.get("white")))
        );
        sendMessage(playersByColor.get("white"), "black", messageForOriginalWhite);
        sendMessage(playersByColor.get("black"), "white", messageForOriginalBlack);
    }

    private void handleJoinQueue(WebSocketSession session, User user) throws IOException {
        WaitingPlayer waitingPlayer = waitingQueue.poll();

        if (waitingPlayer == null) {
            addPlayerToQueue(session, user);
        } else {
            createAndAssignGame(waitingPlayer, session, user);
        }
    }

    private void addPlayerToQueue(WebSocketSession session, User user) throws IOException {
        waitingQueue.add(new WaitingPlayer(session, user));
        Message<PromptMessage> waitingMessage =
                new Message<>("waiting", new PromptMessage("Waiting for an opponent..."));
        sendMessage(session, waitingMessage);
    }

    private void createAndAssignGame(WaitingPlayer waitingPlayer, WebSocketSession session, User user) throws IOException {
        WebSocketSession waitingSession = waitingPlayer.session();
        String newGameId = createGame();

        addToSessionsByGame(newGameId, waitingSession, session);
        addToColorAssignments(newGameId, waitingSession, session);
        addToUserBySessions(waitingSession, waitingPlayer.user(), session, user);

        Message<JoinMessage> waitingPlayerResponse = new Message<>(
                "Game created",
                new JoinMessage(newGameId, "white", new User(user.getUsername()))
        );
        Message<JoinMessage> sessionPlayerResponse = new Message<>(
                "Game created",
                new JoinMessage(newGameId, "black", new User(waitingPlayer.user().getUsername()))
        );

        sendMessage(waitingSession, "white", waitingPlayerResponse);
        sendMessage(session, "black", sessionPlayerResponse);
    }

    private Optional<String> getAssignedColor(String gameId, WebSocketSession session) throws IOException {
        if (gameId == null) {
            sendError(session, "No game id specified");
            return Optional.empty();
        }
        if (!sessionsByGame.containsKey(gameId)) {
            sendError(session, "Game with id " + gameId + " not found");
            return Optional.empty();
        }
        String assignedColor = colorAssignmentsByGame.get(gameId).get(session);
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
        Message<MoveOutput> moveMessage = new Message<>("move", moveOutput);
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
            Message<PossibleMoves> responseMessage = new Message<>("possibilities", moves);
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
        Message<PossibleMoves> responseMessage = new Message<>("possibilities", moves);
        sendMessage(session, assignedColor, responseMessage);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        waitingQueue.removeIf(waitingPlayer -> waitingPlayer.session().equals(session));
        sessionsByGame.entrySet().removeIf(entry -> entry.getValue().contains(session));
        colorAssignmentsByGame.entrySet().removeIf(entry -> entry.getValue().containsKey(session));
        logger.debug("Connection closed: {}", session.getId());
    }

    private void sendError(WebSocketSession session, String error) throws IOException {
        Message<String> errorMessage = new Message<>("error", error);
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
        Set<WebSocketSession> sessions = sessionsByGame.getOrDefault(gameId, Set.of());
        for (WebSocketSession ws : sessions) {
            if (ws.isOpen()) {
                String wsColor = colorAssignmentsByGame.get(gameId).get(ws);
                sendMessage(ws, wsColor, message);
            }
        }
    }

    private void broadcastGameEnd(String gameId, GameState updatedState) throws IOException {
        Set<WebSocketSession> sessions = sessionsByGame.getOrDefault(gameId, Set.of());
        for (WebSocketSession ws : sessions) {
            if (ws.isOpen()) {
                String wsColor = colorAssignmentsByGame.get(gameId).get(ws);
                Message<GameEnd> gameEndMsg;
                if (updatedState.getWinner() == null) {
                    gameEndMsg = new Message<>("gameEnd", new GameEnd("draw"));
                } else {
                    gameEndMsg = new Message<>("gameEnd", new GameEnd(updatedState.getWinner().toString().toLowerCase()));
                }
                sendMessage(ws, wsColor, gameEndMsg);
            }
        }
    }
}
