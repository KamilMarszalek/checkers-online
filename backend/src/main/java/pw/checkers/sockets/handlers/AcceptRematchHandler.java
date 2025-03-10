package pw.checkers.sockets.handlers;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.message.AcceptRematchMessage;
import pw.checkers.message.GameIdMessage;
import pw.checkers.sockets.services.RematchService;

import java.io.IOException;

@Service
public class AcceptRematchHandler {
    private final RematchService rematchService;

    public AcceptRematchHandler(RematchService rematchService) {
        this.rematchService = rematchService;
    }

    public void handleAcceptRematch(WebSocketSession session, AcceptRematchMessage acceptRematchMessage) throws IOException {
        startRematch(session, acceptRematchMessage);
    }

    private void startRematch(WebSocketSession session, GameIdMessage gameIdMessage) throws IOException {
        rematchService.startRematch(session, gameIdMessage);
    }
}
