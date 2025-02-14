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
import java.util.concurrent.Semaphore;

public class CheckersWebSocketHandler extends TextWebSocketHandler {
    private final GameService gameService;
    private final Semaphore mutex = new Semaphore(1);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Set<WebSocketSession>> sessionsByGame = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> colorAssignmentsByGame = new ConcurrentHashMap<>();
    private final Queue<Map<WebSocketSession, User>> waitingQueue = new ConcurrentLinkedQueue<>();
    public CheckersWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connection established: " + session.getId());
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        Message<Map<String, Object>> rawMessage = objectMapper.readValue(message.getPayload(), new TypeReference<>() {});

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
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(defaultMessage)));
                break;
            }
        }
    }

    private void handleJoinQueue(WebSocketSession session, User user) throws Exception {
        mutex.acquire();
        try {
            Map<WebSocketSession, User> waiting = waitingQueue.poll();

            if (waiting == null) {
                Map<WebSocketSession, User> newWaiting = new ConcurrentHashMap<>();
                newWaiting.put(session, user);
                waitingQueue.add(newWaiting);
                Message<PromptMessage> waitingMessage = new Message<>("waiting", new PromptMessage("Waiting for an opponent..."));
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(waitingMessage)));
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

                Message<JoinMessage> waitingPlayerResponse = new Message<>("Game created", new JoinMessage(newGameId, "white", new User(user.getUsername())));
                Message<JoinMessage> sessionPlayerResponse = new Message<>("Game created", new JoinMessage(newGameId, "black", new User(waiting.get(waitingSession).getUsername())));

                String waitingPlayerJsonResponse = objectMapper.writeValueAsString(waitingPlayerResponse);
                String sessionPlayerJsonResponse = objectMapper.writeValueAsString(sessionPlayerResponse);

                waitingSession.sendMessage(new TextMessage(waitingPlayerJsonResponse));
                session.sendMessage(new TextMessage(sessionPlayerJsonResponse));
            }
        } finally {
            mutex.release();
        }
    }

    private void handleMove(WebSocketSession session, MoveInput moveInput) throws Exception {
        String gameId = moveInput.getGameId();
        if (gameId == null) {
            Message<String> moveMessage = new Message<>("error", "No game id specified");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(moveMessage)));
            return;
        }
        if (!sessionsByGame.containsKey(gameId)) {
            Message<String> moveMessage = new Message<>("error", "Game with id " + gameId + " not found");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(moveMessage)));
            return;
        }
        String assignedColor = colorAssignmentsByGame.get(gameId).get(session.getId());
        if (assignedColor == null) {
            Message<String> moveMessage = new Message<>("error", "You do not belong to this game or no color assigned");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(moveMessage)));
            return;
        }

        MoveOutput moveOutput = gameService.makeMove(gameId, moveInput.getMove(), assignedColor);
        Message<MoveOutput> moveMessage = new Message<>("move", moveOutput);
        GameState updatedState = gameService.getGame(gameId);
        String response = objectMapper.writeValueAsString(moveMessage);
        for (WebSocketSession ws : sessionsByGame.getOrDefault(gameId, Set.of())) {
            if (ws.isOpen()) {
                ws.sendMessage(new TextMessage(response));
                if (updatedState.isFinished()) {
                    String gameEndMessage;
                    if (updatedState.getWinner() == null) {
                        gameEndMessage = objectMapper.writeValueAsString(new Message<>("gameEnd", new GameEnd("draw")));
                    } else {
                        gameEndMessage = objectMapper.writeValueAsString(new Message<>("gameEnd", new GameEnd(updatedState.getWinner())));
                    }
                    ws.sendMessage(new TextMessage(gameEndMessage));
                }
            }
        }


    }

    private void handlePossibilities(WebSocketSession session, PossibilitiesInput possibilitiesInput) throws Exception {
        String gameId = possibilitiesInput.getGameId();
        if (gameId == null) {
            Message<String> errorMessage = new Message<>("error", "No game id specified");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMessage)));
            return;
        }
        if (!sessionsByGame.containsKey(gameId)) {
            Message<String> errorMessage = new Message<>("error", "Game with id " + gameId + " not found");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMessage)));
            return;
        }
        GameState currentState = gameService.getGame(gameId);
        PossibleMoves moves = gameService.getPossibleMoves(currentState, possibilitiesInput.getRow(), possibilitiesInput.getCol());
        Message<PossibleMoves> responseMessage = new Message<>("possibilities", moves);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(responseMessage)));
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        waitingQueue.removeIf(waitingMap -> waitingMap.containsKey(session));
        sessionsByGame.values().forEach(sessions -> sessions.remove(session));
        colorAssignmentsByGame.values().forEach(assignment -> assignment.remove(session.getId()));
        System.out.println("Connection closed: " + session.getId());
    }
}
