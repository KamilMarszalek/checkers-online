package pw.checkers.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import pw.checkers.sockets.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final MessageSender messageSender;
    private final SessionManager sessionManager;
    private final RematchService rematchService;
    private final GameManager gameManager;

    public WebSocketConfig(MessageSender messageSender, SessionManager sessionManager, RematchService rematchService, GameManager gameManager) {
        this.messageSender = messageSender;
        this.sessionManager = sessionManager;
        this.rematchService = rematchService;
        this.gameManager = gameManager;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new CheckersWebSocketHandler(sessionManager, rematchService, messageSender, gameManager), "/ws")
                .setAllowedOrigins("*");
    }
}
