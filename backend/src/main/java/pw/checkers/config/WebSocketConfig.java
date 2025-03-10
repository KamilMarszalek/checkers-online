package pw.checkers.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import pw.checkers.sockets.*;
import pw.checkers.sockets.handlers.*;
import pw.checkers.sockets.services.SessionManager;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final SessionManager sessionManager;
    private final AcceptRematchHandler acceptRematchHandler;
    private final DeclineRematchHandler declineRematchHandler;
    private final JoinQueueHandler joinQueueHandler;
    private final LeaveHandler leaveHandler;
    private final LeaveQueueHandler leaveQueueHandler;
    private final MessageMapper messageMapper;
    private final MoveHandler moveHandler;
    private final PossibilitiesHandler possibilitiesHandler;
    private final RematchRequestHandler rematchRequestHandler;
    private final ResignHandler resignHandler;

    public WebSocketConfig(SessionManager sessionManager, AcceptRematchHandler acceptRematchHandler, DeclineRematchHandler declineRematchHandler, JoinQueueHandler joinQueueHandler, LeaveHandler leaveHandler, LeaveQueueHandler leaveQueueHandler, MessageMapper messageMapper, MoveHandler moveHandler, PossibilitiesHandler possibilitiesHandler, RematchRequestHandler rematchRequestHandler, ResignHandler resignHandler) {
        this.acceptRematchHandler = acceptRematchHandler;
        this.declineRematchHandler = declineRematchHandler;
        this.joinQueueHandler = joinQueueHandler;
        this.leaveHandler = leaveHandler;
        this.leaveQueueHandler = leaveQueueHandler;
        this.messageMapper = messageMapper;
        this.moveHandler = moveHandler;
        this.possibilitiesHandler = possibilitiesHandler;
        this.rematchRequestHandler = rematchRequestHandler;
        this.resignHandler = resignHandler;
        this.sessionManager = sessionManager;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new CheckersWebSocketHandler(
                sessionManager,
                joinQueueHandler,
                leaveQueueHandler,
                moveHandler,
                possibilitiesHandler,
                acceptRematchHandler,
                leaveHandler,
                declineRematchHandler,
                rematchRequestHandler,
                resignHandler,
                messageMapper
                ), "/ws")
                .setAllowedOrigins("*");
    }
}
