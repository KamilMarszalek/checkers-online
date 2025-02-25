package pw.checkers.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import pw.checkers.game.GameService;
import pw.checkers.sockets.CheckersWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final GameService gameService;

    public WebSocketConfig(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new CheckersWebSocketHandler(gameService), "/ws")
                .setAllowedOrigins("*");
    }
}
