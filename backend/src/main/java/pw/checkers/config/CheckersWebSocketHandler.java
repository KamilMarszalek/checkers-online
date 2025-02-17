package pw.checkers.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pw.checkers.pojo.*;
import pw.checkers.service.GameService;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckersWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(CheckersWebSocketHandler.class);

    private final GameService gameService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Set<WebSocketSession>> sessionsByGame = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> colorAssignmentsByGame = new ConcurrentHashMap<>();
    private final Queue<Map<WebSocketSession, User>> waitingQueue = new ConcurrentLinkedQueue<>();

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
            default: {
                Message<String> defaultMessage = new Message<>("error", "Unknown message type: " + rawMessage.getType());
                String defaultMessageJson = objectMapper.writeValueAsString(defaultMessage);
                logger.debug("Error (unknown type) message sent to session {}: {}", session.getId(), defaultMessageJson);
                session.sendMessage(new TextMessage(defaultMessageJson));
                break;
            }
        }
    }

    private void handleJoinQueue(WebSocketSession session, User user) throws Exception {
        Map<WebSocketSession, User> waiting = waitingQueue.poll();

        if (waiting == null) {
            Map<WebSocketSession, User> newWaiting = new ConcurrentHashMap<>();
            newWaiting.put(session, user);
            waitingQueue.add(newWaiting);
            Message<PromptMessage> waitingMessage =
                    new Message<>("waiting", new PromptMessage("Waiting for an opponent..."));
            TextMessage response = new TextMessage(objectMapper.writeValueAsString(waitingMessage));
            logger.debug("Message sent to {}: {}", user.getUsername(), response.getPayload());
            session.sendMessage(response);

        } else {
            WebSocketSession waitingSession = waiting.keySet().iterator().next();
            GameState newGame = gameService.createGame();
            String newGameId = newGame.getGameId();

            sessionsByGame.putIfAbsent(newGameId, ConcurrentHashMap.newKeySet());
            sessionsByGame.get(newGameId).add(waitingSession);
            sessionsByGame.get(newGameId).add(session);

            colorAssignmentsByGame.putIfAbsent(newGameId, new ConcurrentHashMap<>());
            colorAssignmentsByGame.get(newGameId).put(waitingSession.getId(), "white");
            colorAssignmentsByGame.get(newGameId).put(session.getId(), "black");

            Message<JoinMessage> waitingPlayerResponse = new Message<>(
                    "Game created",
                    new JoinMessage(newGameId, "white", new User(user.getUsername()))
            );
            Message<JoinMessage> sessionPlayerResponse = new Message<>(
                    "Game created",
                    new JoinMessage(newGameId, "black", new User(waiting.get(waitingSession).getUsername()))
            );

            String waitingPlayerJsonResponse = objectMapper.writeValueAsString(waitingPlayerResponse);
            String sessionPlayerJsonResponse = objectMapper.writeValueAsString(sessionPlayerResponse);

            logger.debug("Message sent to {}: {}", waiting.get(waitingSession).getUsername(), waitingPlayerJsonResponse);
            waitingSession.sendMessage(new TextMessage(waitingPlayerJsonResponse));

            logger.debug("Message sent to {}: {}", user.getUsername(), sessionPlayerJsonResponse);
            session.sendMessage(new TextMessage(sessionPlayerJsonResponse));
        }
    }

    private void handleMove(WebSocketSession session, MoveInput moveInput) throws Exception {
        String gameId = moveInput.getGameId();

        if (gameId == null) {
            Message<String> moveMessage = new Message<>("error", "No game id specified");
            String moveMessageJson = objectMapper.writeValueAsString(moveMessage);

            logger.debug("Error message (no game ID) sent to session {}: {}", session.getId(), moveMessageJson);
            session.sendMessage(new TextMessage(moveMessageJson));
            return;
        }

        if (!sessionsByGame.containsKey(gameId)) {
            Message<String> moveMessage = new Message<>("error", "Game with id " + gameId + " not found");
            String moveMessageJson = objectMapper.writeValueAsString(moveMessage);

            logger.debug("Error message (game not found) sent to session {}: {}", session.getId(), moveMessageJson);
            session.sendMessage(new TextMessage(moveMessageJson));
            return;
        }

        String assignedColor = colorAssignmentsByGame.get(gameId).get(session.getId());
        if (assignedColor == null) {
            Message<String> moveMessage = new Message<>("error", "You do not belong to this game or no color assigned");
            String moveMessageJson = objectMapper.writeValueAsString(moveMessage);
            logger.debug("Error message (no color assigned) sent to session {}: {}", session.getId(), moveMessageJson);
            session.sendMessage(new TextMessage(moveMessageJson));
            return;
        }

        MoveOutput moveOutput = gameService.makeMove(gameId, moveInput.getMove(), assignedColor);
        Message<MoveOutput> moveMessage = new Message<>("move", moveOutput);
        GameState updatedState = gameService.getGame(gameId);
        String response = objectMapper.writeValueAsString(moveMessage);
        for (WebSocketSession ws : sessionsByGame.getOrDefault(gameId, Set.of())) {
            if (ws.isOpen()) {
                String wsColor = colorAssignmentsByGame.get(gameId).get(ws.getId());
                logger.debug("Message sent to color {} (session {}): {}", wsColor, ws.getId(), response);
                ws.sendMessage(new TextMessage(response));
                if (updatedState.isFinished()) {
                    String gameEndMessage;
                    if (updatedState.getWinner() == null) {
                        gameEndMessage = objectMapper.writeValueAsString(new Message<>("gameEnd", new GameEnd("draw")));
                    } else {
                        gameEndMessage = objectMapper.writeValueAsString(
                                new Message<>("gameEnd", new GameEnd(updatedState.getWinner()))
                        );
                    }
                    logger.debug("Message sent to color {} (session {}): {}", wsColor, ws.getId(), gameEndMessage);
                    ws.sendMessage(new TextMessage(gameEndMessage));
                }
            }
        }
        if (moveMessage.getContent().isHasMoreTakes()) {
            PossibleMoves moves = gameService.getPossibleMoves(
                    updatedState,
                    moveMessage.getContent().getMove().getToRow(),
                    moveMessage.getContent().getMove().getToCol()
            );
            Message<PossibleMoves> responseMessage = new Message<>("possibilities", moves);
            String possibilitiesJson = objectMapper.writeValueAsString(responseMessage);

            logger.debug("Message (possibilities) sent to color {} (session {}): {}", assignedColor, session.getId(), possibilitiesJson);
            session.sendMessage(new TextMessage(possibilitiesJson));
        }
    }

    private void handlePossibilities(WebSocketSession session, PossibilitiesInput possibilitiesInput) throws Exception {
        String gameId = possibilitiesInput.getGameId();
        if (gameId == null) {
            Message<String> errorMessage = new Message<>("error", "No game id specified");
            String errorMessageJson = objectMapper.writeValueAsString(errorMessage);
            logger.debug("Error message (no game ID) sent to session {}: {}", session.getId(), errorMessageJson);
            session.sendMessage(new TextMessage(errorMessageJson));
            return;
        }

        if (!sessionsByGame.containsKey(gameId)) {
            Message<String> errorMessage = new Message<>("error", "Game with id " + gameId + " not found");
            String errorMessageJson = objectMapper.writeValueAsString(errorMessage);

            logger.debug("Error message (game not found) sent to session {}: {}", session.getId(), errorMessageJson);
            session.sendMessage(new TextMessage(errorMessageJson));
            return;
        }
        GameState currentState = gameService.getGame(gameId);
        PossibleMoves moves = gameService.getPossibleMoves(
                currentState,
                possibilitiesInput.getRow(),
                possibilitiesInput.getCol()
        );

        Message<PossibleMoves> responseMessage = new Message<>("possibilities", moves);
        String response = objectMapper.writeValueAsString(responseMessage);

        String wsColor = colorAssignmentsByGame.get(gameId).get(session.getId());
        logger.debug("Message (possibilities) sent to color {} (session {}): {}", wsColor, session.getId(), response);
        session.sendMessage(new TextMessage(response));
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        waitingQueue.removeIf(waitingMap -> waitingMap.containsKey(session));
        sessionsByGame.values().forEach(sessions -> sessions.remove(session));
        colorAssignmentsByGame.values().forEach(assignment -> assignment.remove(session.getId()));
        logger.debug("Connection closed: {}", session.getId());
    }
}
