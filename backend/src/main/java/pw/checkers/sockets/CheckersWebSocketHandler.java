package pw.checkers.sockets;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pw.checkers.data.GameState;
import pw.checkers.messages.*;
import pw.checkers.service.GameService;
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

    private final GameService gameService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Set<WebSocketSession>> sessionsByGame = new ConcurrentHashMap<>();
    private final Map<String, Map<WebSocketSession, String>> colorAssignmentsByGame = new ConcurrentHashMap<>();
    private final Map<WebSocketSession, User> usersBySessions = new ConcurrentHashMap<>();
    private final Queue<WaitingPlayer> waitingQueue = new ConcurrentLinkedQueue<>();
    private final Map<String, Set<WebSocketSession>> rematchRequests = new ConcurrentHashMap<>();

    public CheckersWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
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

        switch (rawMessage.getType()) {
            case "joinQueue": {
                Map<String, Object> content = rawMessage.getContent();
                Object userObj = content.get("user");
                User user = objectMapper.convertValue(userObj, User.class);
                handleJoinQueue(session, user);
                break;
            }
            case "move": {
                MoveInput moveInput = objectMapper.convertValue(rawMessage.getContent(), MoveInput.class);
                handleMove(session, moveInput);
                break;
            }
            case "possibilities": {
                PossibilitiesInput possibilitiesInput = objectMapper.convertValue(rawMessage.getContent(), PossibilitiesInput.class);
                handlePossibilities(session, possibilitiesInput);
                break;
            }
            case "rematch request": {
                RematchRequest rematchRequest = objectMapper.convertValue(rawMessage.getContent(), RematchRequest.class);
                proposeRematch(session, rematchRequest);
                break;
            }
            case "accept rematch": {
                RematchRequest rematchRequest = objectMapper.convertValue(rawMessage.getContent(), RematchRequest.class);
                startRematch(session, rematchRequest);
                break;
            }
            case "decline rematch", "leave": {
                RematchRequest rematchRequest = objectMapper.convertValue(rawMessage.getContent(), RematchRequest.class);
                cleanGameHistory(session, rematchRequest);
                break;
            }
            default: {
                Message<String> defaultMessage = new Message<>("error", "Unknown message type: " + rawMessage.getType());
                sendMessage(session, defaultMessage);
                break;
            }
        }
    }

    private void cleanGameHistory(WebSocketSession session, RematchRequest rematchRequest) {
        String gameId = rematchRequest.getGameId();
        colorAssignmentsByGame.remove(gameId);
        usersBySessions.remove(session);
        sessionsByGame.remove(gameId);
        gameService.removeGame(gameId);
    }

    private void proposeRematch(WebSocketSession session, RematchRequest rematchRequest) throws IOException {
        String gameId = rematchRequest.getGameId();
        Set<WebSocketSession> sessions = sessionsByGame.get(gameId);
        if (sessions == null || sessions.isEmpty()) {
            Message<String> message = new Message<>("error", "Opponent has already left the game");
            sendMessage(session, message);
            return;
        }

        rematchRequests.putIfAbsent(gameId, ConcurrentHashMap.newKeySet());
        Set<WebSocketSession> rematchSet = rematchRequests.get(gameId);

        synchronized (rematchSet) {
            rematchSet.add(session);
            if (rematchSet.size() == sessions.size()) {
                startRematch(session, rematchRequest);
                rematchRequests.remove(gameId);
                return;
            }
        }

        Optional<WebSocketSession> opponent = sessions.stream()
                .filter(s -> !s.equals(session))
                .findFirst();
        if (opponent.isPresent()) {
            Message<RematchRequest> message =
                    new Message<>("rematch proposition", new RematchRequest(gameId));
            sendMessage(opponent.get(), message);
        } else {
            Message<String> message =
                    new Message<>("error", "Opponent has already left the game");
            sendMessage(session, message);
        }

    }

    private void startRematch(WebSocketSession session, RematchRequest rematchRequest) throws IOException {
        String gameId = rematchRequest.getGameId();
        Map<WebSocketSession, String> gamePlayers = colorAssignmentsByGame.get(gameId);
        if (gamePlayers == null) {
            Message<String> message = new Message<>("error", "Opponent has already left the game");
            sendMessage(session, message);
            return;
        }
        Map<String, WebSocketSession> playersByColor = gamePlayers.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        colorAssignmentsByGame.remove(gameId);
        sessionsByGame.remove(gameId);
        GameState newGame = gameService.createGame();
        String newGameId = newGame.getGameId();

        sessionsByGame.putIfAbsent(newGameId, ConcurrentHashMap.newKeySet());
        sessionsByGame.get(newGameId).add(playersByColor.get("white"));
        sessionsByGame.get(newGameId).add(playersByColor.get("black"));

        colorAssignmentsByGame.putIfAbsent(newGameId, new ConcurrentHashMap<>());
        colorAssignmentsByGame.get(newGameId).put(playersByColor.get("white"), "black");
        colorAssignmentsByGame.get(newGameId).put(playersByColor.get("black"), "white");

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
            waitingQueue.add(new WaitingPlayer(session, user));
            Message<PromptMessage> waitingMessage =
                    new Message<>("waiting", new PromptMessage("Waiting for an opponent..."));
            sendMessage(session, waitingMessage);
        } else {
            WebSocketSession waitingSession = waitingPlayer.session();
            GameState newGame = gameService.createGame();
            String newGameId = newGame.getGameId();

            sessionsByGame.putIfAbsent(newGameId, ConcurrentHashMap.newKeySet());
            sessionsByGame.get(newGameId).add(waitingSession);
            sessionsByGame.get(newGameId).add(session);

            colorAssignmentsByGame.putIfAbsent(newGameId, new ConcurrentHashMap<>());
            colorAssignmentsByGame.get(newGameId).put(waitingSession, "white");
            colorAssignmentsByGame.get(newGameId).put(session, "black");

            usersBySessions.put(waitingSession, waitingPlayer.user());
            usersBySessions.put(session, user);

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
    }

    private void handleMove(WebSocketSession session, MoveInput moveInput) throws IOException {
        String gameId = moveInput.getGameId();
        if (gameId == null) {
            sendError(session, "No game id specified");
            return;
        }
        if (!sessionsByGame.containsKey(gameId)) {
            sendError(session, "Game with id " + gameId + " not found");
            return;
        }
        String assignedColor = colorAssignmentsByGame.get(gameId).get(session);
        if (assignedColor == null) {
            sendError(session, "You do not belong to this game or no color assigned");
            return;
        }

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
        if (gameId == null) {
            sendError(session, "No game id specified");
            return;
        }
        if (!sessionsByGame.containsKey(gameId)) {
            sendError(session, "Game with id " + gameId + " not found");
            return;
        }
        GameState currentState = gameService.getGame(gameId);
        PossibleMoves moves = gameService.getPossibleMoves(
                currentState,
                possibilitiesInput.getRow(),
                possibilitiesInput.getCol()
        );
        Message<PossibleMoves> responseMessage = new Message<>("possibilities", moves);
        String wsColor = colorAssignmentsByGame.get(gameId).get(session);
        sendMessage(session, wsColor, responseMessage);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        waitingQueue.removeIf(waitingPlayer -> waitingPlayer.session().equals(session));
        sessionsByGame.values().forEach(sessions -> sessions.remove(session));
        colorAssignmentsByGame.values().forEach(assignment -> assignment.remove(session));
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
                    gameEndMsg = new Message<>("gameEnd", new GameEnd(updatedState.getWinner()));
                }
                sendMessage(ws, wsColor, gameEndMsg);
            }
        }
    }
}
