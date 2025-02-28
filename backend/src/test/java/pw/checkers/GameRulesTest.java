package pw.checkers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pw.checkers.data.GameState;
import pw.checkers.data.Piece;
import pw.checkers.data.enums.Color;
import pw.checkers.data.enums.PieceType;
import pw.checkers.game.GameRules;
import pw.checkers.message.Move;
import pw.checkers.message.PossibleMoves;

public class GameRulesTest {

    private GameRules gameRules;

    @BeforeEach
    public void setUp() {
        gameRules = new GameRules();
    }

    /**
     * Creates an empty game state with 8x8 board and default settings.
     */
    private GameState createEmptyGameState() {
        GameState state = new GameState();
        Piece[][] board = new Piece[8][8];
        state.setBoard(board);
        state.setLastCaptureRow(null);
        state.setLastCaptureCol(null);
        state.setNoCapturesCounter(0);
        state.setNumberOfPositions(new HashMap<>());
        state.setCurrentPlayer(Color.WHITE);
        state.setWhitePiecesLeft(12);
        state.setBlackPiecesLeft(12);
        return state;
    }

    @Test
    public void testGetPossibleMovesForEmptyCell() {
        GameState state = createEmptyGameState();
        PossibleMoves moves = gameRules.getPossibleMoves(state, 3, 3);
        assertTrue(moves.getMoves().isEmpty(), "Empty cell should yield no moves");
    }

    @Test
    public void testGetPossibleMovesForWhitePawnNormalMove() {
        GameState state = createEmptyGameState();
        // Place a white pawn (non-king) at (5,2)
        Piece whitePawn = new Piece(Color.WHITE, PieceType.PAWN);
        state.getBoard()[5][2] = whitePawn;

        // Expect two normal moves (diagonally down-left and down-right) if within bounds.
        PossibleMoves moves = gameRules.getPossibleMoves(state, 5, 2);
        assertFalse(moves.getMoves().isEmpty(), "White pawn should have normal moves");
        // Check that moves are one step diagonally.
        moves.getMoves().forEach(m -> {
            int rowDiff = (5 - m.getRow());
            int colDiff = Math.abs(2 - m.getCol());
            assertEquals(1, rowDiff, "Normal move should be one row away");
            assertEquals(1, colDiff, "Normal move should be one column away");
        });

        //Check if there are only 2 moves available
        assertEquals(moves.getMoves().size(), 2, "White pawn should have two moves");
    }

    @Test
    public void testGetPossibleMovesForBlackPawnNormalMove() {
        GameState state = createEmptyGameState();
        // Place a black pawn (non-king) at (5,2)
        Piece whitePawn = new Piece(Color.BLACK, PieceType.PAWN);
        state.getBoard()[5][2] = whitePawn;

        // Expect two normal moves (diagonally up-left and upright) if within bounds.
        PossibleMoves moves = gameRules.getPossibleMoves(state, 5, 2);
        assertFalse(moves.getMoves().isEmpty(), "White pawn should have normal moves");
        // Check that moves are one step diagonally.
        moves.getMoves().forEach(m -> {
            int rowDiff = (m.getRow() - 5);
            int colDiff = Math.abs(2 - m.getCol());
            assertEquals(1, rowDiff, "Normal move should be one row away");
            assertEquals(1, colDiff, "Normal move should be one column away");
        });

        //Check if there are only 2 moves available
        assertEquals(moves.getMoves().size(), 2, "Black pawn should have two moves");
    }

    @Test
    public void testGetPossibleMovesForWhitePawnCaptureMove() {
        GameState state = createEmptyGameState();
        // Place a white pawn at (5,2) and a black pawn at (4,3) to be captured.
        Piece whitePawn = new Piece(Color.WHITE, PieceType.PAWN);
        Piece blackPawn = new Piece(Color.BLACK, PieceType.PAWN);
        state.getBoard()[5][2] = whitePawn;
        state.getBoard()[4][3] = blackPawn;

        // The landing square (3,4) should be empty.
        PossibleMoves moves = gameRules.getPossibleMoves(state, 5, 2);
        // Expect that at least one move is a capture move (distance of 2).
        boolean foundCapture = moves.getMoves().stream()
                .anyMatch(m -> (5 - m.getRow() == 2) && (m.getCol() - 2 == 2));
        assertTrue(foundCapture, "Capture move should be available");
        assertEquals(moves.getMoves().size(), 1, "There should be one move");
        assertEquals(moves.getMoves().getFirst().getRow(), 3, "The landing row should be 3");
        assertEquals(moves.getMoves().getFirst().getCol(), 4, "The landing col should be 2");
    }

    @Test
    public void testGetPossibleMovesForBlackPawnCaptureMove() {
        GameState state = createEmptyGameState();
        // Place a black pawn at (4,3) and a white pawn at (5,2) to be captured.
        Piece whitePawn = new Piece(Color.WHITE, PieceType.PAWN);
        Piece blackPawn = new Piece(Color.BLACK, PieceType.PAWN);
        state.getBoard()[5][2] = whitePawn;
        state.getBoard()[4][3] = blackPawn;

        // The landing square (6,1) should be empty.
        PossibleMoves moves = gameRules.getPossibleMoves(state, 4, 3);
        // Expect that at least one move is a capture move (distance of 2).
        boolean foundCapture = moves.getMoves().stream()
                .anyMatch(m -> m.getRow() - 4 == 2 && 3 - m.getCol() == 2);
        assertTrue(foundCapture, "Capture move should be available");
        assertEquals(moves.getMoves().size(), 1, "There should be one move");
        assertEquals(moves.getMoves().getFirst().getRow(), 6, "The landing row should be 3");
        assertEquals(moves.getMoves().getFirst().getCol(), 1, "The landing col should be 2");
    }

    @Test
    public void testGetPossibleMovesForWhiteKingNormalMove() {
        GameState state = createEmptyGameState();
        // Place a white king at (5,2)
        Piece whitePawn = new Piece(Color.WHITE, PieceType.KING);
        state.getBoard()[5][2] = whitePawn;

        // Expect four normal moves (diagonally left and right) if within bounds.
        PossibleMoves moves = gameRules.getPossibleMoves(state, 5, 2);
        assertFalse(moves.getMoves().isEmpty(), "White pawn should have normal moves");
        // Check that moves are one step diagonally.
        moves.getMoves().forEach(m -> {
            int rowDiff = Math.abs(5 - m.getRow());
            int colDiff = Math.abs(2 - m.getCol());
            assertEquals(1, rowDiff, "Normal move should be one row away");
            assertEquals(1, colDiff, "Normal move should be one column away");
        });

        //Check if there are only 4 moves available
        assertEquals(moves.getMoves().size(), 4, "White pawn should have two moves");
    }

    @Test
    public void testGetPossibleMovesForBlackKingNormalMove() {
        GameState state = createEmptyGameState();
        // Place a black king at (5,2)
        Piece whitePawn = new Piece(Color.BLACK, PieceType.KING);
        state.getBoard()[5][2] = whitePawn;

        // Expect four normal moves (diagonally left and right) if within bounds.
        PossibleMoves moves = gameRules.getPossibleMoves(state, 5, 2);
        assertFalse(moves.getMoves().isEmpty(), "White pawn should have normal moves");
        // Check that moves are one step diagonally.
        moves.getMoves().forEach(m -> {
            int rowDiff = Math.abs(5 - m.getRow());
            int colDiff = Math.abs(2 - m.getCol());
            assertEquals(1, rowDiff, "Normal move should be one row away");
            assertEquals(1, colDiff, "Normal move should be one column away");
        });

        //Check if there are only 4 moves available
        assertEquals(moves.getMoves().size(), 4, "White pawn should have two moves");
    }

    @Test
    public void testGetPossibleMovesForWhiteKingCaptureMove() {
        GameState state = createEmptyGameState();
        // Place a white king at (5,2)
        Piece whiteKing = new Piece(Color.WHITE, PieceType.KING);
        Piece blackPawn1 = new Piece(Color.BLACK, PieceType.PAWN);
        Piece blackPawn2 = new Piece(Color.BLACK, PieceType.KING);
        state.getBoard()[5][2] = whiteKing;
        state.getBoard()[4][3] = blackPawn1;
        state.getBoard()[6][1] = blackPawn2;

        // Expect two captures - the king can go backwards
        PossibleMoves moves = gameRules.getPossibleMoves(state, 5, 2);
        assertFalse(moves.getMoves().isEmpty(), "White king should have two capture moves");
        // Check that moves are two-step diagonally.
        moves.getMoves().forEach(m -> {
            int rowDiff = Math.abs(5 - m.getRow());
            int colDiff = Math.abs(2 - m.getCol());
            assertEquals(2, rowDiff, "Normal move should be one row away");
            assertEquals(2, colDiff, "Normal move should be one column away");
        });

        //Check if there are only 2 moves available
        assertEquals(moves.getMoves().size(), 2, "White pawn should have two moves");
    }

    @Test
    public void testGetPossibleMovesForBlackKingCaptureMove() {
        GameState state = createEmptyGameState();
        // Place a black king at (5,2)
        Piece blackKing = new Piece(Color.BLACK, PieceType.KING);
        Piece whitePawn1 = new Piece(Color.WHITE, PieceType.PAWN);
        Piece whitePawn2 = new Piece(Color.WHITE, PieceType.KING);
        state.getBoard()[5][2] = blackKing;
        state.getBoard()[4][3] = whitePawn1;
        state.getBoard()[6][1] = whitePawn2;

        // Expect two captures - the king can go backwards
        PossibleMoves moves = gameRules.getPossibleMoves(state, 5, 2);
        assertFalse(moves.getMoves().isEmpty(), "White king should have two capture moves");
        // Check that moves are two-step diagonally.
        moves.getMoves().forEach(m -> {
            int rowDiff = Math.abs(5 - m.getRow());
            int colDiff = Math.abs(2 - m.getCol());
            assertEquals(2, rowDiff, "Normal move should be one row away");
            assertEquals(2, colDiff, "Normal move should be one column away");
        });

        //Check if there are only 2 moves available
        assertEquals(moves.getMoves().size(), 2, "White pawn should have two moves");
    }

    @Test
    public void testGetPossibleMovesForPawnBlockedByAllies(){
        GameState state = createEmptyGameState();
        // Place a white pawns at (5,2), (6,3), (5,4)
        Piece whitePawn1 = new Piece(Color.WHITE, PieceType.PAWN);
        Piece whitePawn2 = new Piece(Color.WHITE, PieceType.PAWN);
        Piece whitePawn3 = new Piece(Color.WHITE, PieceType.PAWN);
        state.getBoard()[5][2] = whitePawn1;
        state.getBoard()[6][3] = whitePawn2;
        state.getBoard()[5][4] = whitePawn3;

        // Expect no moves - pawn is blocked by his allies
        PossibleMoves moves = gameRules.getPossibleMoves(state, 6, 3);
        assertTrue(moves.getMoves().isEmpty(), "There should be no moves available");
    }

    @Test
    public void testPlayerHasMoves() {
        GameState state = createEmptyGameState();
        // Place a white pawn so that white has available moves.
        Piece whitePawn = new Piece(Color.WHITE, PieceType.PAWN);
        state.getBoard()[5][2] = whitePawn;
        assertTrue(gameRules.playerHasMoves(state, Color.WHITE), "White should have moves");
    }

    @Test
    public void testHasMoreTakes() {
        GameState state = createEmptyGameState();
        // Setup for a multi-capture scenario.
        Piece whitePawn = new Piece(Color.WHITE, PieceType.PAWN);
        Piece blackPawn1 = new Piece(Color.BLACK, PieceType.PAWN);
        Piece blackPawn2 = new Piece(Color.BLACK, PieceType.PAWN);
        // Place white pawn at (5,2)
        state.getBoard()[5][2] = whitePawn;
        // Place a first opponent piece to capture: at (4,3)
        state.getBoard()[4][3] = blackPawn1;
        // Place a second opponent piece so that after capturing to (3,4) another capture is available
        state.getBoard()[2][5] = blackPawn2;
        // Place white pawn at (3,4) - simulating real move
        state.getBoard()[3][4] = whitePawn;
        // Test hasMoreTakes from first capture move.
        Move firstCapture = new Move(5, 2, 3, 4);

        assertTrue(gameRules.hasMoreTakes(state, firstCapture), "Should have additional capture moves");
    }

    @Test
    public void testIsDrawFiftyMoveViolation() {
        GameState state = createEmptyGameState();
        state.setNoCapturesCounter(50);
        assertTrue(gameRules.isDraw(state), "Game should be a draw due to fifty move rule");
    }

    @Test
    public void testIsDrawBothPlayersHaveNoMoves(){
        GameState state = createEmptyGameState();
        Piece whitePawn = new Piece(Color.WHITE, PieceType.PAWN);
        Piece blackPawn = new Piece(Color.BLACK, PieceType.PAWN);
        state.getBoard()[0][1] = whitePawn;
        state.getBoard()[7][0] = blackPawn;
        assertTrue(gameRules.isDraw(state), "Game should be a draw due to no moves available");
    }

    @Test
    public void testIsDrawThreefoldRepetition() {
        GameState state = createEmptyGameState();
        // Simulate threefold repetition by setting numberOfPositions.
        Map<String, Integer> positions = new HashMap<>();

        // Place a piece so that moves exist (so only the repetition is the draw trigger).
        Piece whitePawn = new Piece(Color.WHITE, PieceType.PAWN);
        state.getBoard()[5][2] = whitePawn;
        positions.put(state.boardToString(), 3);
        state.setNumberOfPositions(positions);
        // For this test, assume boardToString returns "testBoard"
        // You might need to override or simulate this in your test environment.
        assertTrue(gameRules.isDraw(state), "Game should be a draw due to threefold repetition");
    }

    @Test
    public void testHasSomebodyWonByPiecesCount() {
        GameState state = createEmptyGameState();
        // Test winning condition: if black pieces left equals 0.
        state.setBlackPiecesLeft(0);
        assertTrue(gameRules.hasSomebodyWon(state), "Game should be won if black pieces are 0");
    }


    @Test
    public void testHasSomebodyWonByOpponentBeingBlocked() {
        GameState state = createEmptyGameState();
        // Black player turn - white made previous move
        state.setCurrentPlayer(Color.WHITE);
        Piece blackPawn = new Piece(Color.BLACK, PieceType.PAWN);
        Piece whitePawn1 = new Piece(Color.WHITE, PieceType.PAWN);
        Piece whitePawn2 = new Piece(Color.WHITE, PieceType.PAWN);
        Piece whitePawn3 = new Piece(Color.WHITE, PieceType.PAWN);
        state.getBoard()[0][1] = blackPawn;
        state.getBoard()[1][0] = whitePawn1;
        state.getBoard()[1][2] = whitePawn2;
        state.getBoard()[2][3] = whitePawn3;
        assertTrue(gameRules.hasSomebodyWon(state), "Game should be won if black is blocked");
    }
}
