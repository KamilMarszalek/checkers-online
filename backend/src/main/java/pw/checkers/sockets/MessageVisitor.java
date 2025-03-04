package pw.checkers.sockets;

import org.springframework.web.socket.WebSocketSession;
import pw.checkers.message.*;

import java.io.IOException;

public interface MessageVisitor {
    void visit(JoinQueueMessage message, WebSocketSession session) throws IOException;
    void visit(LeaveQueueMessage message, WebSocketSession session);
    void visit(MoveInput message, WebSocketSession session) throws IOException;
    void visit(PossibilitiesInput message, WebSocketSession session) throws IOException;
    void visit(RematchRequestMessage message, WebSocketSession session) throws IOException;
    void visit(AcceptRematchMessage message, WebSocketSession session) throws IOException;
    void visit(DeclineRematchMessage message, WebSocketSession session) throws IOException;
    void visit(LeaveMessage message, WebSocketSession session) throws IOException;
}
