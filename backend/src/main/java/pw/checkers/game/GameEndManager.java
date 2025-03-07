package pw.checkers.game;

import org.springframework.stereotype.Service;
import pw.checkers.data.GameState;
import pw.checkers.data.enums.Color;

@Service
public class GameEndManager {
    private final GameRules gameRules;

    public GameEndManager(GameRules gameRules) {
        this.gameRules = gameRules;
    }


    public void setDraw(GameState gameState) {
        gameState.setFinished(true);
        setGameEndReason(gameState, false);
    }

    public void setWinner(GameState gameState) {
        Color currentPlayer = gameState.getCurrentPlayer();
        Color otherPlayer = Color.WHITE.equals(currentPlayer) ? Color.BLACK : Color.WHITE;

        if (gameState.getWhitePiecesLeft() == 0) {
            gameState.setWinner(Color.BLACK);
            gameState.setFinished(true);
            setGameEndReason(gameState, false);
            return;
        } else if (gameState.getBlackPiecesLeft() == 0) {
            gameState.setWinner(Color.WHITE);
            gameState.setFinished(true);
            setGameEndReason(gameState, false);
            return;
        }
        if (!gameRules.playerHasMoves(gameState, otherPlayer) && gameRules.playerHasMoves(gameState, currentPlayer)) {
            gameState.setWinner(currentPlayer);
            gameState.setFinished(true);
            setGameEndReason(gameState, false);
        }
    }

    public void setGameEndReason(GameState gameState, boolean resigned) {

    }
}
