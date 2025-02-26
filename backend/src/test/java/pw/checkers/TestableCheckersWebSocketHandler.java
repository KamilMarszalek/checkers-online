package pw.checkers;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.sockets.*;

public class TestableCheckersWebSocketHandler extends CheckersWebSocketHandler {
    public TestableCheckersWebSocketHandler(SessionManager sessionManager, RematchService rematchService,
                                            MessageSender messageSender, GameManager gameManager) {
        super(sessionManager, rematchService, messageSender, gameManager);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
    }
}