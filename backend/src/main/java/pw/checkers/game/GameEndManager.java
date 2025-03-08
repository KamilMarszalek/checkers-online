package pw.checkers.game;

import org.springframework.stereotype.Service;
import pw.checkers.data.GameState;
import pw.checkers.data.enums.Color;
import pw.checkers.data.enums.GameEndReason;

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
            updateGameState(gameState, Color.BLACK);
            return;
        } else if (gameState.getBlackPiecesLeft() == 0) {
            updateGameState(gameState, Color.WHITE);
            return;
        }
        if (!gameRules.playerHasMoves(gameState, otherPlayer) && gameRules.playerHasMoves(gameState, currentPlayer)) {
            updateGameState(gameState, currentPlayer);
        }
    }

    private void updateGameState(GameState gameState, Color winner) {
        gameState.setWinner(winner);
        gameState.setFinished(true);
        setGameEndReason(gameState, false);
    }

    public void setGameEndReason(GameState gameState, boolean resigned) {
        if (resigned) {
            gameState.setGameEndReason(GameEndReason.RESIGNATION);
            return;
        }
        if (gameState.getWhitePiecesLeft() == 0 || gameState.getBlackPiecesLeft() == 0) {
            gameState.setGameEndReason(GameEndReason.NO_PIECES);
        } else if (!gameRules.playerHasMoves(gameState, Color.BLACK) || !gameRules.playerHasMoves(gameState, Color.WHITE)) {
            gameState.setGameEndReason(GameEndReason.NO_MOVES);
        } else if (gameState.getNoCapturesCounter() >= 50) {
            gameState.setGameEndReason(GameEndReason.FIFTY_MOVES);
        } else if (gameState.getNumberOfPositions().containsValue(3)) {
            gameState.setGameEndReason(GameEndReason.THREEFOLD_REPETITION);
        }
    }
}
