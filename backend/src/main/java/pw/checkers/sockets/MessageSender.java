package pw.checkers.sockets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.data.GameState;
import pw.checkers.message.GameEnd;
import pw.checkers.message.Message;
import pw.checkers.message.PromptMessage;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static pw.checkers.data.enums.MessageType.ERROR;

@Service
public class MessageSender {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);


    public void sendError(WebSocketSession session, String error) throws IOException {
        Message errorMessage = new PromptMessage(ERROR.getValue(), error);
        sendMessage(session, errorMessage);
    }

    public void sendMessage(WebSocketSession session, Message message) throws IOException {
        String messageJson = objectMapper.writeValueAsString(message);
        logger.debug("Message sent to session {}: {}", session.getId(), messageJson);
        session.sendMessage(new TextMessage(messageJson));
    }

    public void sendMessage(WebSocketSession session, String color, Message message) throws IOException {
        String messageJson = objectMapper.writeValueAsString(message);
        logger.debug("Message sent to color {} (session {}): {}", color, session.getId(), messageJson);
        session.sendMessage(new TextMessage(messageJson));
    }

    public void broadcastToGame(Set<WebSocketSession> sessions, Message message, Map<WebSocketSession, String> colorByPlayer) throws IOException {
        for (WebSocketSession ws : sessions) {
            if (ws.isOpen()) {
                String wsColor = colorByPlayer.get(ws);
                sendMessage(ws, wsColor, message);
            }
        }
    }

    public void broadcastGameEnd(Set<WebSocketSession> sessions, GameState updatedState, Map<WebSocketSession, String> colorByPlayer) throws IOException {
        for (WebSocketSession ws : sessions) {
            if (ws.isOpen()) {
                String wsColor = colorByPlayer.get(ws);
                Message gameEndMsg;
                if (updatedState.getWinner() == null) {
                    gameEndMsg = new GameEnd("draw");
                } else {
                    gameEndMsg = new GameEnd(updatedState.getWinner().toString().toLowerCase());
                }
                sendMessage(ws, wsColor, gameEndMsg);
            }
        }
    }
}
