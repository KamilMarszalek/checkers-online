package pw.checkers;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pw.checkers.data.GameState;
import pw.checkers.data.Piece;
import pw.checkers.data.enums.Color;
import pw.checkers.game.GameEndManager;
import pw.checkers.game.GameRules;

public class GameEndManagerTest {

    private GameEndManager gameEndManager;
    private GameRules fakeGameRules;

    @BeforeEach
    public void setUp() {
        fakeGameRules = new GameRules() {
            @Override
            public boolean playerHasMoves(GameState gameState, Color player) {
                return true;
            }
        };

        gameEndManager = new GameEndManager(fakeGameRules);
    }

    /**
     * Helper to create a minimal game state.
     */
    private GameState createGameState() {
        GameState state = new GameState();
        state.setBoard(new Piece[8][8]);
        state.setCurrentPlayer(Color.WHITE);
        state.setWhitePiecesLeft(12);
        state.setBlackPiecesLeft(12);
        state.setFinished(false);
        state.setWinner(null);
        return state;
    }

    @Test
    public void testSetDraw() {
        GameState state = createGameState();
        gameEndManager.setDraw(state);
        assertTrue(state.isFinished(), "Game should be finished when setDraw is called.");
    }

    @Test
    public void testSetWinnerWhenWhitePiecesZero() {
        GameState state = createGameState();
        state.setWhitePiecesLeft(0);
        state.setBlackPiecesLeft(5);
        state.setCurrentPlayer(Color.BLACK);

        gameEndManager.setWinner(state);

        assertTrue(state.isFinished(), "Game should be finished when white pieces are zero.");
        assertEquals(Color.BLACK, state.getWinner(), "Black should be declared winner when white pieces are 0.");
    }

    @Test
    public void testSetWinnerWhenBlackPiecesZero() {
        GameState state = createGameState();
        state.setWhitePiecesLeft(5);
        state.setBlackPiecesLeft(0);
        state.setCurrentPlayer(Color.WHITE);

        gameEndManager.setWinner(state);

        assertTrue(state.isFinished(), "Game should be finished when black pieces are zero.");
        assertEquals(Color.WHITE, state.getWinner(), "White should be declared winner when black pieces are 0.");
    }

    @Test
    public void testSetWinnerWhenNoPiecesZeroButNoMovesForOpponent() {
        // In this test, we simulate the scenario where neither side is out of pieces,
        // but the opponent has no moves.
        GameState state = createGameState();
        state.setWhitePiecesLeft(5);
        state.setBlackPiecesLeft(5);
        state.setCurrentPlayer(Color.WHITE);
        // Override fakeGameRules for this test:
        fakeGameRules = new GameRules() {
            @Override
            public boolean playerHasMoves(GameState gameState, Color player) {
                // Simulate: white (current player) has moves, black (opponent) has none.
                return player == Color.WHITE;
            }
        };
        gameEndManager = new GameEndManager(fakeGameRules);

        gameEndManager.setWinner(state);

        assertTrue(state.isFinished(), "Game should be finished when opponent has no moves.");
        assertEquals(Color.WHITE, state.getWinner(), "Current player should be declared winner when opponent has no moves.");
    }

    @Test
    public void testSetWinnerWhenNoConditionMet() {
        // In this test, neither the pieces count nor the conditions are met.
        // setWinner should not set a winner or finish the game.
        GameState state = createGameState();
        state.setWhitePiecesLeft(5);
        state.setBlackPiecesLeft(5);
        state.setCurrentPlayer(Color.WHITE);

        // For this test, simulate that both players have moves.
        fakeGameRules = new GameRules() {
            @Override
            public boolean playerHasMoves(GameState gameState, Color player) {
                return true;
            }
        };
        gameEndManager = new GameEndManager(fakeGameRules);

        gameEndManager.setWinner(state);

        assertFalse(state.isFinished(), "Game should not be finished if no win/draw condition is met.");
        assertNull(state.getWinner(), "Winner should remain null if no win/draw condition is met.");
    }
}
