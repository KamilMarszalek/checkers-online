package pw.checkers.sockets.handlers;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.message.PossibilitiesInput;
import pw.checkers.message.PossibilitiesOutput;
import pw.checkers.sockets.GameManager;
import pw.checkers.sockets.MessageSender;
import pw.checkers.sockets.SessionManager;

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

    public void handlePossibilities(WebSocketSession session, PossibilitiesInput possibilitiesInput) throws IOException {
        PossibilitiesOutput moves = gameManager.getPossibleMoves(possibilitiesInput, session);
        if (moves == null) return;
        Optional<String> maybeColor = sessionManager.getAssignedColor(possibilitiesInput.getGameId(), session);
        if (maybeColor.isEmpty()) return;
        String assignedColor = maybeColor.get();
        messageSender.sendMessage(session, assignedColor, moves);
    }
}
