package pw.checkers.message;

import org.springframework.web.socket.WebSocketSession;
import pw.checkers.sockets.MessageVisitor;

public interface MessageAccept {
    void accept(MessageVisitor visitor, WebSocketSession session) throws Exception;
}
