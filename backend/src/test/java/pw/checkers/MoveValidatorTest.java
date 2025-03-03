package pw.checkers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pw.checkers.data.GameState;
import pw.checkers.data.Piece;
import pw.checkers.data.enums.Color;
import pw.checkers.data.enums.PieceType;
import pw.checkers.game.BoardManager;
import pw.checkers.game.GameRules;
import pw.checkers.game.MoveValidator;
import pw.checkers.message.Move;
import pw.checkers.message.MoveHelper;
import pw.checkers.message.PossibilitiesOutput;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MoveValidatorTest {
    @Mock
    private BoardManager boardManager;
    @Mock
    private GameRules gameRules;

    private MoveValidator moveValidator;

    @BeforeEach
    public void setUp() {
        moveValidator = new MoveValidator(boardManager, gameRules);
    }

    /**
     * Helper method to create a minimal GameState.
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
        state.setNumberOfPositions(new java.util.HashMap<>());
        return state;
    }

    @Test
    public void testValidateMove_NullGameStateReturnsFalse() {
        Move move = new Move(0, 0, 1, 1);
        assertFalse(moveValidator.validateMove(null, move));
    }

    @Test
    public void testValidateMove_GameFinishedReturnsFalse() {
        GameState state = createGameState();
        state.setFinished(true);
        Move move = new Move(0, 0, 1, 1);
        assertFalse(moveValidator.validateMove(state, move));
    }

    @Test
    public void testValidateMove_NoPieceAtFromReturnsFalse() {
        GameState state = createGameState();
        // board[0][0] remains null
        Move move = new Move(0, 0, 1, 1);
        assertFalse(moveValidator.validateMove(state, move));
    }

    @Test
    public void testValidateMove_PieceWrongColorReturnsFalse() {
        GameState state = createGameState();
        Piece[][] board = state.getBoard();
        // Place a BLACK piece at (0,0) while currentPlayer is WHITE.
        board[0][0] = new Piece(Color.BLACK, PieceType.PAWN);
        Move move = new Move(0, 0, 1, 1);
        assertFalse(moveValidator.validateMove(state, move));
    }

    @Test
    public void testValidateMove_BrokenMultipleCaptureSequenceReturnsFalse() {
        GameState state = createGameState();
        Piece[][] board = state.getBoard();
        // Place a WHITE piece at (2,2)
        board[2][2] = new Piece(Color.WHITE, PieceType.PAWN);
        // Set last capture coordinates to a different cell than the move's from.
        state.setLastCaptureRow(3);
        state.setLastCaptureCol(3);
        Move move = new Move(2, 2, 3, 3);
        // The method brokenMultipleCaptureSequence returns true, so validateMove returns false.
        assertFalse(moveValidator.validateMove(state, move));
    }

        @Test
        public void testValidateMove_ValidMoveReturnsTrue() {
            GameState state = createGameState();
            Piece[][] board = state.getBoard();
            // Place a WHITE pawn at (2,2)
            board[2][2] = new Piece(Color.WHITE, PieceType.PAWN);
            // Ensure no capture sequence is active.
            state.setLastCaptureRow(null);
            state.setLastCaptureCol(null);
            // Prepare a valid move: from (2,2) to (1,1)
            Move move = new Move(2, 2, 1, 1);

            // Create a PossibleMoves instance that includes the expected move.
            PossibilitiesOutput possibilitiesOutput = new PossibilitiesOutput();
            List<MoveHelper> movesList = new ArrayList<>();
            movesList.add(new MoveHelper(1, 1));
            possibilitiesOutput.setMoves(movesList);

            // Stub gameRules.getPossibleMoves to return our possibleMoves for cell (2,2)
            when(gameRules.getPossibleMoves(state, 2, 2)).thenReturn(possibilitiesOutput);

            boolean result = moveValidator.validateMove(state, move);
            assertTrue(result, "The move should be valid since it is in possibleMoves.");
        }

    @Test
    public void testValidateMove_ValidMoveReturnsFalseIfNotInPossibleMoves() {
        GameState state = createGameState();
        Piece[][] board = state.getBoard();
        // Place a WHITE pawn at (2,2)
        board[2][2] = new Piece(Color.WHITE, PieceType.PAWN);
        state.setLastCaptureRow(null);
        state.setLastCaptureCol(null);
        Move move = new Move(2, 2, 1, 1);

        // Return a PossibleMoves that does NOT include the move (1,1)
        PossibilitiesOutput possibilitiesOutput = new PossibilitiesOutput();
        possibilitiesOutput.setMoves(new ArrayList<>()); // empty list
        when(gameRules.getPossibleMoves(state, 2, 2)).thenReturn(possibilitiesOutput);

        boolean result = moveValidator.validateMove(state, move);
        assertFalse(result, "The move should be invalid if it is not in the list of possible moves.");
    }
}
