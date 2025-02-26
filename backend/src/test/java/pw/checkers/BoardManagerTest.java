package pw.checkers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import pw.checkers.data.GameState;
import pw.checkers.data.Piece;
import pw.checkers.data.enums.Color;
import pw.checkers.data.enums.PieceType;
import pw.checkers.game.BoardManager;
import pw.checkers.game.GameEndManager;
import pw.checkers.game.GameRules;
import pw.checkers.message.Move;
import pw.checkers.message.MoveOutput;

@ExtendWith(MockitoExtension.class)
public class BoardManagerTest {

    @Mock
    private GameEndManager gameEndManager;

    @Mock
    private GameRules gameRules;

    private BoardManager boardManager;

    @BeforeEach
    public void setUp() {
        boardManager = new BoardManager(gameEndManager, gameRules);
    }

    /**
     * Helper to create a minimal GameState.
     */
    private GameState createGameState() {
        GameState state = new GameState();
        Piece[][] board = new Piece[8][8];
        state.setBoard(board);
        state.setFinished(false);
        state.setCurrentPlayer(Color.WHITE);
        state.setWhitePiecesLeft(12);
        state.setBlackPiecesLeft(12);
        state.setLastCaptureRow(null);
        state.setLastCaptureCol(null);
        state.setNumberOfPositions(new HashMap<>());
        return state;
    }

    @Test
    public void testInitializeBoard() {
        GameState state = createGameState();
        boardManager.initializeBoard(state);
        Piece[][] board = state.getBoard();
        // Check first three rows for black pawns on playable squares.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 1) {
                    assertNotNull(board[row][col], "Playable square at (" + row + "," + col + ") should have a piece");
                    assertEquals(PieceType.PAWN, board[row][col].getType());
                    assertEquals(Color.BLACK, board[row][col].getColor());
                } else {
                    assertNull(board[row][col], "Non-playable square at (" + row + "," + col + ") should be null");
                }
            }
        }
        // Check last three rows for white pawns on playable squares.
        for (int row = 5; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 1) {
                    assertNotNull(board[row][col], "Playable square at (" + row + "," + col + ") should have a piece");
                    assertEquals(PieceType.PAWN, board[row][col].getType());
                    assertEquals(Color.WHITE, board[row][col].getColor());
                } else {
                    assertNull(board[row][col], "Non-playable square at (" + row + "," + col + ") should be null");
                }
            }
        }
    }

    @Test
    public void testMakeMove_NormalMove() {
        // Set up a normal move (non-capture) for a white pawn.
        GameState state = createGameState();
        Piece[][] board = state.getBoard();
        // Place white pawn at (5,2)
        Piece whitePawn = new Piece(Color.WHITE, PieceType.PAWN);
        board[5][2] = whitePawn;
        // Ensure no forced capture is active.
        state.setLastCaptureRow(null);
        state.setLastCaptureCol(null);
        // Prepare a move from (5,2) to (4,3)
        Move move = new Move(5, 2, 4, 3);
        MoveOutput output = new MoveOutput();
        output.setMove(move);

        // Stub gameRules.hasMoreTakes to return false,
        // hasSomebodyWon and isDraw to return false.
        when(gameRules.hasMoreTakes(state, move)).thenReturn(false);
        when(gameRules.hasSomebodyWon(state)).thenReturn(false);
        when(gameRules.isDraw(state)).thenReturn(false);

        // Execute the move.
        MoveOutput result = boardManager.makeMove(state, output);

        // Validate board update.
        assertNull(board[5][2], "Source cell should be cleared");
        assertEquals(whitePawn, board[4][3], "Pawn should be moved to destination");

        // Validate turn switching.
        assertEquals(Color.BLACK, state.getCurrentPlayer(), "Turn should switch to black");
        assertEquals("black", result.getCurrentTurn());
        assertEquals("white", result.getPreviousTurn());
    }

    @Test
    public void testMakeMove_CaptureMove() {
        // Set up a capture move for a white pawn.
        GameState state = createGameState();
        Piece[][] board = state.getBoard();
        // Place white pawn at (5,2) and black pawn at (4,3)
        Piece whitePawn = new Piece(Color.WHITE, PieceType.PAWN);
        Piece blackPawn = new Piece(Color.BLACK, PieceType.PAWN);
        board[5][2] = whitePawn;
        board[4][3] = blackPawn;
        state.setLastCaptureRow(null);
        state.setLastCaptureCol(null);
        // Create a move from (5,2) to (3,4) (a jump capture)
        Move move = new Move(5, 2, 3, 4);
        MoveOutput output = new MoveOutput();
        output.setMove(move);

        // Stub gameRules.hasMoreTakes to return true (so additional capture is available)
        when(gameRules.hasMoreTakes(state, move)).thenReturn(true);

        // Execute move.
        MoveOutput result = boardManager.makeMove(state, output);

        // Verify that the black pawn is captured.
        assertNull(board[4][3], "Captured piece should be removed from the board");
        // Verify that white pawn moved to destination.
        assertEquals(whitePawn, board[3][4], "White pawn should move to landing square");
        // Verify that moveOutput indicates a capture.
        assertTrue(result.isCaptured(), "MoveOutput should indicate a capture");
        // Verify captured piece coordinates.
        assertEquals(4, result.getCapturedPiece().getRow());
        assertEquals(3, result.getCapturedPiece().getCol());
        // Because hasMoreTakes is true, turn should not switch.
        assertEquals("white", result.getCurrentTurn());
        assertEquals("white", result.getPreviousTurn());
        // Also, verify that last capture coordinates were set.
        assertEquals(3, state.getLastCaptureRow());
        assertEquals(4, state.getLastCaptureCol());
    }

    @Test
    public void testMakeMove_Promotion() {
        // Set up promotion: white pawn moves to row 0.
        GameState state = createGameState();
        Piece[][] board = state.getBoard();
        // Place white pawn at (1,2)
        Piece whitePawn = new Piece(Color.WHITE, PieceType.PAWN);
        board[1][2] = whitePawn;
        state.setCurrentPlayer(Color.WHITE);
        state.setLastCaptureRow(null);
        state.setLastCaptureCol(null);

        // Create a move from (1,2) to (0,3)
        Move move = new Move(1, 2, 0, 3);
        MoveOutput output = new MoveOutput();
        output.setMove(move);

        // Stub gameRules.hasMoreTakes to return false and no win/draw.
        when(gameRules.hasMoreTakes(state, move)).thenReturn(false);
        when(gameRules.hasSomebodyWon(state)).thenReturn(false);
        when(gameRules.isDraw(state)).thenReturn(false);

        // Execute move.
        boardManager.makeMove(state, output);

        // Verify that the white pawn is promoted to KING.
        assertEquals(PieceType.KING, board[0][3].getType(), "Pawn should be promoted to KING");
        // Verify source cell is cleared.
        assertNull(board[1][2]);
        // Turn switching should occur.
        assertEquals(Color.BLACK, state.getCurrentPlayer());
    }

    @Test
    public void testMakeMove_GameEnd() {
        // Set up a move that triggers a win/draw condition.
        GameState state = createGameState();
        Piece[][] board = state.getBoard();
        // Place white pawn at (5,2)
        Piece whitePawn = new Piece(Color.WHITE, PieceType.PAWN);
        board[5][2] = whitePawn;
        state.setCurrentPlayer(Color.WHITE);
        state.setLastCaptureRow(null);
        state.setLastCaptureCol(null);

        // Create a normal move from (5,2) to (4,3)
        Move move = new Move(5, 2, 4, 3);
        MoveOutput output = new MoveOutput();
        output.setMove(move);

        // Stub gameRules: no more takes, and suppose somebody won.
        when(gameRules.hasMoreTakes(state, move)).thenReturn(false);
        when(gameRules.hasSomebodyWon(state)).thenReturn(true);
        // Also stub isDraw false.
        lenient().when(gameRules.isDraw(state)).thenReturn(false);

        // Execute move.
        boardManager.makeMove(state, output);

        // Verify that gameEndManager.setWinner was called.
        verify(gameEndManager, times(1)).setWinner(state);
    }
}
