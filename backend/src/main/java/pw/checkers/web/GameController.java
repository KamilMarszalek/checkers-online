package pw.checkers.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pw.checkers.pojo.GameState;
import pw.checkers.service.GameService;

@RestController
@RequestMapping("/game")
@Tag(name="GameController", description = "Controller to create and retrieve games")
public class GameController {
    private final GameService gameService;
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("")
    public ResponseEntity<GameState> createGame() {
        GameState gameState = gameService.createGame();
        return ResponseEntity.ok(gameState);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameState> getGame(@PathVariable String gameId) {
        GameState gameState = gameService.getGame(gameId);
        if (gameState == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(gameState);
    }
}
