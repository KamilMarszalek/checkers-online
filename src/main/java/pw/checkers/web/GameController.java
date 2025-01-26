package pw.checkers.web;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import pw.checkers.pojo.GameState;
import pw.checkers.service.GameService;

@Controller
public class GameController {
    private final GameService gameService;
    public GameController(GameService gameService) {
        this.gameService = gameService;
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
