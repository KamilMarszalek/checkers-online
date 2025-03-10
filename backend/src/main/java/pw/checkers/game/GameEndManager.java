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
        if (gameState.getWhitePiecesLeft() == 0) {
            updateGameState(gameState, Color.BLACK);
        } else if (gameState.getBlackPiecesLeft() == 0) {
            updateGameState(gameState, Color.WHITE);
        } else if (opponentCannotMove(gameState, currentPlayer)) {
            updateGameState(gameState, currentPlayer);
        }
    }


    private boolean opponentCannotMove(GameState gameState, Color currentPlayer) {
        Color otherPlayer = Color.WHITE.equals(currentPlayer) ? Color.BLACK : Color.WHITE;
        return !gameRules.playerHasMoves(gameState, otherPlayer) && gameRules.playerHasMoves(gameState, currentPlayer);
    }


    private void updateGameState(GameState gameState, Color winner) {
        gameState.setWinner(winner);
        gameState.setFinished(true);
        setGameEndReason(gameState, false);
    }

    private GameEndReason determineGameEndReason(GameState gameState, boolean resigned) {
        if (resigned) {
            return GameEndReason.RESIGNATION;
        }
        if (gameState.getWhitePiecesLeft() == 0 || gameState.getBlackPiecesLeft() == 0) {
            return GameEndReason.NO_PIECES;
        }
        if (!gameRules.playerHasMoves(gameState, Color.BLACK) || !gameRules.playerHasMoves(gameState, Color.WHITE)) {
            return GameEndReason.NO_MOVES;
        }
        if (gameState.getNoCapturesCounter() >= 50) {
            return GameEndReason.FIFTY_MOVES;
        }
        if (gameState.getNumberOfPositions().containsValue(3)) {
            return GameEndReason.THREEFOLD_REPETITION;
        }
        return null;
    }

    public void setGameEndReason(GameState gameState, boolean resigned) {
        GameEndReason reason = determineGameEndReason(gameState, resigned);
        if (reason != null) {
            gameState.setGameEndReason(reason);
        }
    }
}
