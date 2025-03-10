package pw.checkers.sockets.handlers;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.message.DeclineRematchMessage;
import pw.checkers.message.GameIdMessage;
import pw.checkers.message.Message;
import pw.checkers.message.PromptMessage;
import pw.checkers.sockets.services.GameManager;
import pw.checkers.sockets.services.MessageSender;
import pw.checkers.sockets.services.RematchService;
import pw.checkers.sockets.services.SessionManager;

import java.io.IOException;
import java.util.Optional;

import static pw.checkers.data.enums.MessageType.REJECTION;
import static pw.checkers.utils.Constants.OPPONENT_LEFT;
import static pw.checkers.utils.Constants.OPPONENT_REJECTED;

@Service
public class DeclineRematchHandler {
    private final GameManager gameManager;
    private final RematchService rematchService;
    private final SessionManager sessionManager;
    private final MessageSender messageSender;

    public DeclineRematchHandler(GameManager gameManager, RematchService rematchService, SessionManager sessionManager, MessageSender messageSender) {
        this.gameManager = gameManager;
        this.rematchService = rematchService;
        this.sessionManager = sessionManager;
        this.messageSender = messageSender;
    }

    public void handleDeclineRematch(WebSocketSession session, DeclineRematchMessage declineRematchMessage) throws IOException {
        sendRejection(session, declineRematchMessage);
        gameManager.cleanGameHistory(declineRematchMessage);
        rematchService.removeFromRematchRequests(declineRematchMessage.getGameId());
        sessionManager.removeUsersBySessionEntry(session);
    }

    private void sendRejection(WebSocketSession session, GameIdMessage gameIdMessage) throws IOException {
        String gameId = gameIdMessage.getGameId();

        Optional<WebSocketSession> opponent = sessionManager.getOpponent(gameId, session);

        if (opponent.isPresent()) {
            sendRejectionHelper(session, OPPONENT_REJECTED);
        } else {
            sendRejectionHelper(session, OPPONENT_LEFT);
        }
    }

    private void sendRejectionHelper(WebSocketSession session, String status) throws IOException {
        Message message = new PromptMessage(REJECTION.getValue(), status);
        messageSender.sendMessage(session, message);
    }

}
