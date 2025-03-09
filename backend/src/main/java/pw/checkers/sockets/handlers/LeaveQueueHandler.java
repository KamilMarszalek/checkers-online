package pw.checkers.sockets.handlers;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.message.User;
import pw.checkers.sockets.SessionManager;

@Service
public class LeaveQueueHandler {
    private final SessionManager sessionManager;

    public LeaveQueueHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void handleLeaveQueue(WebSocketSession session, User user) {
        sessionManager.removeWaitingPlayerFromQueue(session, user);
    }
}
