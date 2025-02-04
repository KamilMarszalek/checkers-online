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
        GameState currentState = gameService.getGame(gameId);

        switch (wsMessage.getType()) {
            case "join":
                String joinResponse = objectMapper.writeValueAsString(currentState);
                session.sendMessage(new TextMessage(joinResponse));
                break;
            case "move":
                MoveOutput move = gameService.makeMove(gameId, wsMessage.getMove());
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
        System.out.println("Connection closed: " + session.getId());
    }
}
