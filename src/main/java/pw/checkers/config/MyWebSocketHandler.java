package pw.checkers.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pw.checkers.pojo.GameState;
import pw.checkers.pojo.MoveRequest;
import pw.checkers.service.GameService;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MyWebSocketHandler extends TextWebSocketHandler {
    private final GameService gameService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Set<WebSocketSession>> sessionsByGame = new ConcurrentHashMap<>();

    public MyWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connection established: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        MoveRequest moveRequest = objectMapper.readValue(message.getPayload(), MoveRequest.class);
        sessionsByGame
                .computeIfAbsent(moveRequest.getGameId(), k -> ConcurrentHashMap.newKeySet())
                .add(session);

        GameState updatedState = gameService.makeMove(moveRequest.getGameId(), moveRequest.getMove());
        String responseJson = objectMapper.writeValueAsString(updatedState);
        for (WebSocketSession ws : sessionsByGame.getOrDefault(moveRequest.getGameId(), Set.of())) {
            if (ws.isOpen()) {
                ws.sendMessage(new TextMessage(responseJson));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        sessionsByGame.values().forEach(sessions -> sessions.remove(session));
        System.out.println("Connection closed: " + session.getId());
    }
}
