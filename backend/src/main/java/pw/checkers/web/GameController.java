package pw.checkers.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pw.checkers.data.GameState;
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
    @Operation(summary = "Create and set up new game of checkers")
    @ApiResponse(responseCode = "200", description = "Game successfully created",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameState.class)))
    public ResponseEntity<GameState> createGame() {
        GameState gameState = gameService.createGame();
        return ResponseEntity.ok(gameState);
    }

    @GetMapping("/{gameId}")
    @Operation(summary = "Retrieve game by id")
    @ApiResponse(responseCode = "200", description = "Game state successfully delivered",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GameState.class)))
    public ResponseEntity<GameState> getGame(@PathVariable String gameId) {
        GameState gameState = gameService.getGame(gameId);
        if (gameState == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(gameState);
    }
}
