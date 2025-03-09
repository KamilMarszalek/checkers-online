package pw.checkers;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.sockets.*;
import pw.checkers.sockets.handlers.*;

public class TestableCheckersWebSocketHandler extends CheckersWebSocketHandler {
    public TestableCheckersWebSocketHandler(SessionManager sessionManager, JoinQueueHandler joinQueueHandler, LeaveQueueHandler leaveQueueHandler, MoveHandler moveHandler, PossibilitiesHandler possibilitiesHandler, AcceptRematchHandler acceptRematchHandler, LeaveHandler leaveHandler, DeclineRematchHandler declineRematchHandler, RematchRequestHandler rematchRequestHandler, ResignHandler resignHandler, MessageMapper messageMapper) {
        super(sessionManager,
                joinQueueHandler,
                leaveQueueHandler,
                moveHandler,
                possibilitiesHandler,
                acceptRematchHandler,
                leaveHandler,
                declineRematchHandler,
                rematchRequestHandler,
                resignHandler,
                messageMapper);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
    }
}