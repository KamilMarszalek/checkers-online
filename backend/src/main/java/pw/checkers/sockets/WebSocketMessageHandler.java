package pw.checkers.sockets;

import org.springframework.web.socket.WebSocketSession;
import pw.checkers.message.Message;

import java.io.IOException;
import java.util.Map;

@FunctionalInterface
public interface WebSocketMessageHandler {
    void handle(WebSocketSession session, Message<Map<String, Object>> rawMessage) throws IOException;
}
