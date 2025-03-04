package pw.checkers.sockets;

import org.springframework.web.socket.WebSocketSession;
import pw.checkers.message.Message;

import java.io.IOException;

@FunctionalInterface
public interface WebSocketMessageHandler {
    void handle(WebSocketSession session, Message rawMessage) throws IOException;
}
