package pw.checkers.message;

import org.springframework.web.socket.WebSocketSession;
import pw.checkers.sockets.MessageVisitor;

public class RematchRequestMessage extends GameIdMessage implements MessageAccept {
    @Override
    public void accept(MessageVisitor visitor, WebSocketSession session) throws Exception {
        visitor.visit(this, session);
    }
}
