package pw.checkers.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import pw.checkers.game.GameService;
import pw.checkers.sockets.CheckersWebSocketHandler;
import pw.checkers.sockets.SessionManager;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final GameService gameService;
    private final SessionManager sessionManager;

    public WebSocketConfig(GameService gameService, SessionManager sessionManager) {
        this.gameService = gameService;
        this.sessionManager = sessionManager;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new CheckersWebSocketHandler(gameService, sessionManager), "/ws")
                .setAllowedOrigins("*");
    }
}
