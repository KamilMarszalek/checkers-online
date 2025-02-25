package pw.checkers.game;

import org.springframework.stereotype.Service;
import pw.checkers.data.GameState;
import pw.checkers.data.enums.PieceColor;

@Service
public class GameEndManager {
    private final GameRules gameRules;

    public GameEndManager(GameRules gameRules) {
        this.gameRules = gameRules;
    }


    public void setDraw(GameState gameState) {
        gameState.setFinished(true);
    }

    public void setWinner(GameState gameState) {
        PieceColor currentPlayer = gameState.getCurrentPlayer();
        PieceColor otherPlayer = PieceColor.WHITE.equals(currentPlayer) ? PieceColor.BLACK : PieceColor.WHITE;

        if (gameState.getWhitePiecesLeft() == 0) {
            gameState.setWinner(PieceColor.BLACK);
            gameState.setFinished(true);
            return;
        } else if (gameState.getBlackPiecesLeft() == 0) {
            gameState.setWinner(PieceColor.WHITE);
            gameState.setFinished(true);
            return;
        }
        if (!gameRules.playerHasMoves(gameState, otherPlayer) && gameRules.playerHasMoves(gameState, currentPlayer)) {
            gameState.setWinner(currentPlayer);
            gameState.setFinished(true);
        }
    }
}
