package pw.checkers.sockets.handlers;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.message.LeaveMessage;
import pw.checkers.sockets.GameManager;
import pw.checkers.sockets.SessionManager;

import java.io.IOException;

@Service
public class LeaveHandler {
    private final GameManager gameManager;
    private final SessionManager sessionManager;

    public LeaveHandler(GameManager gameManager, SessionManager sessionManager) {
        this.gameManager = gameManager;
        this.sessionManager = sessionManager;
    }

    public void handleLeave(WebSocketSession session, LeaveMessage leaveMessage) throws IOException {
        gameManager.cleanGameHistory(leaveMessage);
        sessionManager.removeUsersBySessionEntry(session);
    }
}

