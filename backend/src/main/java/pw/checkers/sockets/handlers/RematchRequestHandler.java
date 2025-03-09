package pw.checkers.sockets.handlers;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.message.GameIdMessage;
import pw.checkers.message.RematchRequestMessage;
import pw.checkers.sockets.RematchService;

import java.io.IOException;

@Service
public class RematchRequestHandler {
    private final RematchService rematchService;

    public RematchRequestHandler(RematchService rematchService) {
        this.rematchService = rematchService;
    }

    public void handleRematchRequest(WebSocketSession session, RematchRequestMessage rematchRequestMessage) throws IOException {
        proposeRematch(session, rematchRequestMessage);
    }

    private void proposeRematch(WebSocketSession session, GameIdMessage gameIdMessage) throws IOException {
        rematchService.proposeRematch(session, gameIdMessage);
    }
}
