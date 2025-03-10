package pw.checkers.sockets.handlers;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.message.PossibilitiesInputMessage;
import pw.checkers.message.PossibilitiesOutputMessage;
import pw.checkers.sockets.services.GameManager;
import pw.checkers.sockets.services.MessageSender;
import pw.checkers.sockets.services.SessionManager;

import java.io.IOException;
import java.util.Optional;

@Service
public class PossibilitiesHandler {
    private final GameManager gameManager;
    private final SessionManager sessionManager;
    private final MessageSender messageSender;

    public PossibilitiesHandler(GameManager gameManager, SessionManager sessionManager, MessageSender messageSender) {
        this.gameManager = gameManager;
        this.sessionManager = sessionManager;
        this.messageSender = messageSender;
    }

    public void handlePossibilities(WebSocketSession session, PossibilitiesInputMessage possibilitiesInputMessage) throws IOException {
        PossibilitiesOutputMessage moves = gameManager.getPossibleMoves(possibilitiesInputMessage, session);
        if (moves == null) return;
        Optional<String> maybeColor = sessionManager.getAssignedColor(possibilitiesInputMessage.getGameId(), session);
        if (maybeColor.isEmpty()) return;
        String assignedColor = maybeColor.get();
        messageSender.sendMessage(session, assignedColor, moves);
    }
}
