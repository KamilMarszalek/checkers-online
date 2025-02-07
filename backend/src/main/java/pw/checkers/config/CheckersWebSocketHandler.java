package pw.checkers.config;

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

public class CheckersWebSocketHandler extends TextWebSocketHandler {
    private final GameService gameService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Set<WebSocketSession>> sessionsByGame = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> colorAssignmentsByGame = new ConcurrentHashMap<>();
    private final Queue<WebSocketSession> waitingQueue = new ConcurrentLinkedQueue<>();
    public CheckersWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connection established: " + session.getId());
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        WsMessage wsMessage = objectMapper.readValue(message.getPayload(), WsMessage.class);
        String messageType = wsMessage.getType();

        switch (messageType) {
            case "joinQueue": {
                handleJoinQueue(session);
                break;
            }
            case "move": {
                handleMove(session, wsMessage);
                break;
            }
            case "possibilities": {
                handlePossibilities(session, wsMessage);
                break;
            }
            default: {
                session.sendMessage(new TextMessage("Unknown message type: " + messageType));
                break;
            }
        }
    }

    private void handleJoinQueue(WebSocketSession session) throws Exception {
        WebSocketSession waiting = waitingQueue.poll();

        if (waiting == null) {
            waitingQueue.add(session);
            session.sendMessage(new TextMessage("Waiting for an opponent..."));
        } else {
            GameState newGame = gameService.createGame();
            String newGameId = newGame.getGameId();
            sessionsByGame.putIfAbsent(newGameId, ConcurrentHashMap.newKeySet());
            sessionsByGame.get(newGameId).add(waiting);
            sessionsByGame.get(newGameId).add(session);

            colorAssignmentsByGame.putIfAbsent(newGameId, new ConcurrentHashMap<>());
            colorAssignmentsByGame.get(newGameId).put(waiting.getId(), "white");
            colorAssignmentsByGame.get(newGameId).put(session.getId(), "black");

            JoinMessage waitingPlayerResponse = new JoinMessage("Game created", newGameId, "white");
            JoinMessage sessionPlayerResponse = new JoinMessage("Game created",newGameId, "black");

            String waitingPlayerJsonResponse = objectMapper.writeValueAsString(waitingPlayerResponse);
            String sessionPlayerJsonResponse = objectMapper.writeValueAsString(sessionPlayerResponse);

            waiting.sendMessage(new TextMessage(waitingPlayerJsonResponse));
            session.sendMessage(new TextMessage(sessionPlayerJsonResponse));
        }
    }

    private void handleMove(WebSocketSession session, WsMessage wsMessage) throws Exception {
        String gameId = wsMessage.getGameId();
        if (gameId == null) {
            session.sendMessage(new TextMessage("No gameId specified"));
            return;
        }
        if (!sessionsByGame.containsKey(gameId)) {
            session.sendMessage(new TextMessage("Game with id " + gameId + " not found"));
            return;
        }
        String assignedColor = colorAssignmentsByGame.get(gameId).get(session.getId());
        if (assignedColor == null) {
            session.sendMessage(new TextMessage("You do not belong to this game or no color assigned"));
            return;
        }

        MoveOutput moveOutput = gameService.makeMove(gameId, wsMessage.getMove(), assignedColor);
        GameState updatedState = gameService.getGame(gameId);
        String response = objectMapper.writeValueAsString(moveOutput);
        for (WebSocketSession ws : sessionsByGame.getOrDefault(gameId, Set.of())) {
            if (ws.isOpen()) {
                ws.sendMessage(new TextMessage(response));
                if (updatedState.isFinished()) {
                    String gameEndMessage;
                    if (updatedState.getWinner() == null) {
                        gameEndMessage = objectMapper.writeValueAsString(new GameEnd("draw"));
                    } else {
                        gameEndMessage = objectMapper.writeValueAsString(new GameEnd(updatedState.getWinner()));
                    }
                    ws.sendMessage(new TextMessage(gameEndMessage));
                }
            }
        }


    }

    private void handlePossibilities(WebSocketSession session, WsMessage wsMessage) throws Exception {
        String gameId = wsMessage.getGameId();
        if (gameId == null) {
            session.sendMessage(new TextMessage("No gameId specified"));
            return;
        }
        if (!sessionsByGame.containsKey(gameId)) {
            session.sendMessage(new TextMessage("Game with id " + gameId + " not found"));
            return;
        }
        GameState currentState = gameService.getGame(gameId);
        PossibleMoves moves = gameService.getPossibleMoves(currentState, wsMessage.getRow(), wsMessage.getCol());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(moves)));
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        waitingQueue.remove(session);
        sessionsByGame.values().forEach(sessions -> sessions.remove(session));
        colorAssignmentsByGame.values().forEach(assignment -> assignment.remove(session.getId()));
        System.out.println("Connection closed: " + session.getId());
    }
}
