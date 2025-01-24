package pw.checkers.web;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pw.checkers.pojo.GameState;
import pw.checkers.pojo.Move;
import pw.checkers.service.GameService;

@Controller
public class GameController {
    private final GameService gameService;
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @MessageMapping("/game/{gameId}/move")
    @SendTo("topic/game/{gameId}")
    public GameState processMove(@DestinationVariable String gameId, Move move) {
        return gameService.makeMove(gameId, move);
    }

    @PostMapping("/api/game")
    public ResponseEntity<GameState> createGame() {
        GameState gameState = gameService.createGame();
        return ResponseEntity.ok(gameState);
    }

    @GetMapping("/api/game/{gameId}")
    public ResponseEntity<GameState> getGame(@PathVariable String gameId) {
        GameState gameState = gameService.getGame(gameId);
        if (gameState == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(gameState);
    }
}
