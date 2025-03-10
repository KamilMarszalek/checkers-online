package pw.checkers.sockets.handlers;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.data.GameState;
import pw.checkers.data.enums.GameEndReason;
import pw.checkers.message.ResignMessage;
import pw.checkers.sockets.services.GameManager;
import pw.checkers.sockets.services.MessageSender;
import pw.checkers.sockets.services.SessionManager;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
public class ResignHandler {
    private final SessionManager sessionManager;
    private final GameManager gameManager;
    private final MessageSender messageSender;

    public ResignHandler(SessionManager sessionManager, GameManager gameManager, MessageSender messageSender) {
        this.sessionManager = sessionManager;
        this.gameManager = gameManager;
        this.messageSender = messageSender;
    }

    public void handleResign(WebSocketSession session, ResignMessage resignMessage) throws IOException {
        Map<WebSocketSession, String> colorsBySession = sessionManager.getColorAssignments(resignMessage.getGameId());
        String assignedColor = sessionManager.getAssignedColorByGameIdAndSession(resignMessage.getGameId(), session);
        String opponentColor = assignedColor.equals("white") ? "black" : "white";
        Set<WebSocketSession> sessions = sessionManager.getSessionsByGameId(resignMessage.getGameId());
        GameState updatedState = gameManager.setGameEnd(resignMessage, opponentColor);
        updatedState.setGameEndReason(GameEndReason.RESIGNATION);
        messageSender.broadcastGameEnd(sessions, updatedState, colorsBySession);
    }
}
