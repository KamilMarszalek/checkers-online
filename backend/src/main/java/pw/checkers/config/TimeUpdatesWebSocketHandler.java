package pw.checkers.config;

import org.springframework.web.socket.handler.TextWebSocketHandler;
import pw.checkers.service.GameService;

public class TimeUpdatesWebSocketHandler extends TextWebSocketHandler {
    private final GameService gameService;
    public TimeUpdatesWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }


}
