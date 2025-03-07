package pw.checkers.sockets;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.data.GameState;
import pw.checkers.data.enums.Color;
import pw.checkers.game.GameEndManager;
import pw.checkers.game.GameService;
import pw.checkers.message.*;

import java.io.IOException;
import java.util.Optional;

@Service
public class GameManager {
    private final GameService gameService;
    private final SessionManager sessionManager;
    private final GameEndManager gameEndManager;

    public GameManager(GameService gameService, SessionManager sessionManager, GameEndManager gameEndManager) {
        this.gameService = gameService;
        this.sessionManager = sessionManager;
        this.gameEndManager = gameEndManager;
    }

    public void cleanGameHistory(GameIdMessage gameIdMessage){
        String gameId = gameIdMessage.getGameId();
        sessionManager.removeGameFromMaps(gameId);
        gameService.deleteGame(gameId);
    }

    public String createGame() {
        GameState newGame = gameService.createGame();
        return newGame.getGameId();
    }

    public MoveOutput makeMove(String gameId, Move move, String color) {
        return gameService.makeMove(gameId, move, color);
    }

    public GameState getGame(String gameId) {
        return gameService.getGame(gameId);
    }

    public PossibilitiesOutput getPossibleMoves(PossibilitiesInput possibilitiesInput, WebSocketSession session) throws IOException {
        String gameId = possibilitiesInput.getGameId();
        Optional<String> maybeColor = sessionManager.getAssignedColor(gameId, session);
        if (maybeColor.isEmpty()) return null;
        GameState currentState = getGame(gameId);
        return gameService.getPossibleMoves(
                currentState,
                possibilitiesInput.getRow(),
                possibilitiesInput.getCol()
        );
    }

    public GameState setGameEnd(GameIdMessage gameIdMessage, String winner) {
        String gameId = gameIdMessage.getGameId();
        GameState gameState = getGame(gameId);
        gameState.setFinished(true);
        Color player = winner.equals("white") ? Color.WHITE : Color.BLACK;
        gameState.setWinner(player);
        return gameState;
    }

    public void setGameEndReason(String gameId, boolean resigned) {
        gameEndManager.setGameEndReason(getGame(gameId), resigned);
    }
}
