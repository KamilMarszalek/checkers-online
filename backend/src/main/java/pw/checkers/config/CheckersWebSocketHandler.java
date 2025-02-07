package pw.checkers.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pw.checkers.pojo.*;
import pw.checkers.service.GameService;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CheckersWebSocketHandler extends TextWebSocketHandler {
    private final GameService gameService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Set<WebSocketSession>> sessionsByGame = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> colorAssignmentsByGame = new ConcurrentHashMap<>();
    public CheckersWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connection established: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        WsMessage wsMessage = objectMapper.readValue(message.getPayload(), WsMessage.class);
        String gameId = wsMessage.getGameId();
        sessionsByGame
                .computeIfAbsent(gameId, k -> ConcurrentHashMap.newKeySet())
                .add(session);
        Map<String, String> colorAssignments = colorAssignmentsByGame
                .computeIfAbsent(gameId, k -> new ConcurrentHashMap<>());

        GameState currentState = gameService.getGame(gameId);

        switch (wsMessage.getType()) {
            case "join":
                if (!colorAssignments.containsKey(session.getId())) {
                    boolean whiteTaken = colorAssignments.containsValue("white");
                    boolean blackTaken = colorAssignments.containsValue("black");

                    if (!whiteTaken) {
                        colorAssignments.put(session.getId(), "white");
                    } else if (!blackTaken) {
                        colorAssignments.put(session.getId(), "black");
                    } else {
                        session.sendMessage(new TextMessage("Game is full"));
                        return;
                    }
                }
                String joinResponse = objectMapper.writeValueAsString(currentState);
                session.sendMessage(new TextMessage(joinResponse));
                break;
            case "move":
                String assignedColor = colorAssignments.get(session.getId());
                if (assignedColor == null) {
                    session.sendMessage(new TextMessage("You have not joined the game or no color assigned"));
                    return;
                }
                MoveOutput move = gameService.makeMove(gameId, wsMessage.getMove(), assignedColor);
                String moveResponse = objectMapper.writeValueAsString(move);
                for (WebSocketSession ws : sessionsByGame.getOrDefault(gameId, Set.of())) {
                    if (ws.isOpen()) {
                        ws.sendMessage(new TextMessage(moveResponse));
                    }
                }
                break;
            case "possibilities":
                PossibleMoves possibleMoves = gameService.getPossibleMoves(currentState, wsMessage.getRow(), wsMessage.getCol());
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(possibleMoves)));
                break;
            default:
                session.sendMessage(new TextMessage("Unknown message type"));
                break;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        sessionsByGame.values().forEach(sessions -> sessions.remove(session));
        colorAssignmentsByGame.values().forEach(assignment -> assignment.remove(session.getId()));
        System.out.println("Connection closed: " + session.getId());
    }
}
