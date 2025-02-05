package pw.checkers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import pw.checkers.pojo.*;
import pw.checkers.service.GameService;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTests {
    @InjectMocks
    private GameService gameService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void createGame_ShouldInitializeGameCorrectly() {
        GameState gameState = gameService.createGame();
        assertNotNull(gameState);
        assertNotNull(gameState.getGameId());
        assertEquals("white", gameState.getCurrentPlayer());
        assertFalse(gameState.isFinished());
        assertNull(gameState.getWinner());
        assertNotNull(gameState.getBoard());
        Piece[][] board = gameState.getBoard();
        assertNotNull(board);

        int boardSize = 8;
        assertEquals(boardSize, board.length);

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                Piece piece = board[row][col];

                if (row < 3) {
                    if ((row + col) % 2 == 1) {
                        assertNotNull(piece);
                        assertEquals(PieceColor.BLACK, piece.getColor());
                        assertEquals(PieceType.PAWN, piece.getType());
                    } else {
                        assertNull(piece);
                    }
                } else if (row > 4) {
                    if ((row + col) % 2 == 1) {
                        assertNotNull(piece);
                        assertEquals(PieceColor.WHITE, piece.getColor());
                        assertEquals(PieceType.PAWN, piece.getType());
                    } else {
                        assertNull(piece);
                    }
                } else {
                    assertNull(piece);
                }
            }
        }
    }

    @Test
    void getGame_ShouldReturnGameById() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();

        GameState fetchedGame = gameService.getGame(gameId);
        assertNotNull(fetchedGame);
        assertEquals(gameId, fetchedGame.getGameId());
    }

    @Test
    void getGame_ShouldReturnNullForNonExistingGame() {
        assertNull(gameService.getGame(UUID.randomUUID().toString()));
    }

    @Test
    void makeMove_ShouldReturnNullForInvalidMove() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();

        MoveInput invalidMove = new MoveInput(0, 0, 1, 1);
        MoveOutput result = gameService.makeMove(gameId, invalidMove);

        assertNull(result);
    }

    @Test
    void makeMove_ShouldChangeTurnWhenMoveIsValid() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();

        MoveInput validMove = new MoveInput(5, 2, 4, 3);
        MoveOutput result = gameService.makeMove(gameId, validMove);

        assertNotNull(result);
        assertFalse(result.isCaptured());
        assertEquals(result.getFromColumn(), validMove.getFromColumn());
        assertEquals(result.getFromRow(), validMove.getFromRow());
        assertEquals(result.getToColumn(), validMove.getToColumn());
        assertEquals(result.getToRow(), validMove.getToRow());
        assertNull(result.getCapturedCol());
        assertNull(result.getCapturedRow());
        assertEquals("black", gameService.getGame(gameId).getCurrentPlayer());
    }

    @Test
    void makeMove_ShouldHandleSingleCaptureCorrectly() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();
        Piece[][] board = gameState.getBoard();

        board[4][3] = new Piece(PieceColor.BLACK, PieceType.PAWN);
        board[5][2] = new Piece(PieceColor.WHITE, PieceType.PAWN);

        MoveInput captureMove = new MoveInput(5, 2, 3, 4);
        MoveOutput result = gameService.makeMove(gameId, captureMove);

        assertNotNull(result);
        assertEquals(result.getFromColumn(), captureMove.getFromColumn());
        assertEquals(result.getFromRow(), captureMove.getFromRow());
        assertEquals(result.getToColumn(), captureMove.getToColumn());
        assertEquals(result.getToRow(), captureMove.getToRow());
        assertEquals(result.getCapturedRow(), 4);
        assertEquals(result.getCapturedCol(), 3);
        assertFalse(result.isHasMoreTakes());
        assertTrue(result.isCaptured());
        assertNull(board[4][3]);
    }

    @Test
    void makeMove_ShouldHandleMultipleCaptureCorrectly() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();
        Piece[][] board = gameState.getBoard();
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board.length; col++) {
                board[row][col] = null;
            }
        }

        board[4][3] = new Piece(PieceColor.BLACK, PieceType.PAWN);
        board[2][3] = new Piece(PieceColor.BLACK, PieceType.PAWN);
        board[5][2] = new Piece(PieceColor.WHITE, PieceType.PAWN);

        MoveInput captureMove = new MoveInput(5, 2, 3, 4);
        MoveOutput result = gameService.makeMove(gameId, captureMove);

        assertNotNull(result);
        assertEquals(result.getFromColumn(), captureMove.getFromColumn());
        assertEquals(result.getFromRow(), captureMove.getFromRow());
        assertEquals(result.getToColumn(), captureMove.getToColumn());
        assertEquals(result.getToRow(), captureMove.getToRow());
        assertEquals(result.getCapturedRow(), 4);
        assertEquals(result.getCapturedCol(), 3);
        assertTrue(result.isHasMoreTakes());
        assertTrue(result.isCaptured());
        assertNull(board[4][3]);
        assertEquals(gameService.getGame(gameId).getCurrentPlayer(), "white");
    }

    @Test
    void makeMove_ShouldPromotePawnToKing() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();
        Piece[][] board = gameState.getBoard();
        board[0][3] = null;

        board[1][2] = new Piece(PieceColor.WHITE, PieceType.PAWN);
        MoveInput promotionMove = new MoveInput(1, 2, 0, 3);

        gameService.makeMove(gameId, promotionMove);

        assertEquals(PieceType.KING, board[0][3].getType());
    }

    @Test
    void makeMove_ShouldReturnNull_WhenMovingPieceOfWrongColor() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();

        MoveInput blackMove = new MoveInput(2, 1, 3, 2);
        MoveOutput result = gameService.makeMove(gameId, blackMove);

        assertNull(result);
    }

    @Test
    void makeMove_ShouldReturnNull_WhenFromFieldIsEmpty() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();

        MoveInput emptyFieldMove = new MoveInput(4, 4, 3, 3);
        MoveOutput result = gameService.makeMove(gameId, emptyFieldMove);

        assertNull(result);
    }

    @Test
    void makeMove_ShouldReturnNull_WhenGameIsAlreadyFinished() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();
        gameState.setFinished(true);

        MoveInput anyMove = new MoveInput(5, 2, 4, 3);
        MoveOutput result = gameService.makeMove(gameId, anyMove);

        assertNull(result);
    }

    @Test
    void getPossibleMoves_ShouldReturnOnlyCaptureMoves_IfCaptureExists() {
        GameState gameState = gameService.createGame();
        Piece[][] board = gameState.getBoard();

        for (Piece[] pieces : board) {
            Arrays.fill(pieces, null);
        }
        board[4][3] = new Piece(PieceColor.BLACK, PieceType.PAWN);
        board[5][2] = new Piece(PieceColor.WHITE, PieceType.PAWN);


        PossibleMoves pm = gameService.getPossibleMoves(gameState, 5, 2);

        assertEquals(1, pm.getMoves().size());
        MoveInput move = pm.getMoves().getFirst();
        assertEquals(5, move.getFromRow());
        assertEquals(2, move.getFromColumn());
        assertEquals(3, move.getToRow());
        assertEquals(4, move.getToColumn());
    }

    @Test
    void getPossibleMoves_ShouldReturnAllDirectionsForKing() {
        GameState gameState = gameService.createGame();
        Piece[][] board = gameState.getBoard();

        for (Piece[] pieces : board) {
            Arrays.fill(pieces, null);
        }
        board[4][4] = new Piece(PieceColor.WHITE, PieceType.KING);

        PossibleMoves pm = gameService.getPossibleMoves(gameState, 4, 4);

        assertEquals(4, pm.getMoves().size(), "King in the middle should have 4 possible moves if no captures exist");

        boolean hasUpLeft = pm.getMoves().stream()
                .anyMatch(m -> m.getToRow() == 3 && m.getToColumn() == 3);
        boolean hasUpRight = pm.getMoves().stream()
                .anyMatch(m -> m.getToRow() == 3 && m.getToColumn() == 5);
        boolean hasDownLeft = pm.getMoves().stream()
                .anyMatch(m -> m.getToRow() == 5 && m.getToColumn() == 3);
        boolean hasDownRight = pm.getMoves().stream()
                .anyMatch(m -> m.getToRow() == 5 && m.getToColumn() == 5);

        assertTrue(hasUpLeft);
        assertTrue(hasUpRight);
        assertTrue(hasDownLeft);
        assertTrue(hasDownRight);
    }

    @Test
    void multipleCapturesInOneTurn_ByWhitePawn() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();

        Piece[][] board = gameState.getBoard();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board[r][c] = null;
            }
        }

        board[5][0] = new Piece(PieceColor.WHITE, PieceType.PAWN);

        board[4][1] = new Piece(PieceColor.BLACK, PieceType.PAWN);
        board[2][3] = new Piece(PieceColor.BLACK, PieceType.PAWN);

        MoveInput firstCapture = new MoveInput(5, 0, 3, 2);
        MoveOutput firstOutput = gameService.makeMove(gameId, firstCapture);

        assertNotNull(firstOutput);
        assertTrue(firstOutput.isCaptured());
        assertEquals(4, firstOutput.getCapturedRow());
        assertEquals(1, firstOutput.getCapturedCol());
        assertTrue(firstOutput.isHasMoreTakes());

        MoveInput secondCapture = new MoveInput(3, 2, 1, 4);
        MoveOutput secondOutput = gameService.makeMove(gameId, secondCapture);

        assertNotNull(secondOutput);
        assertTrue(secondOutput.isCaptured());
        assertEquals(2, secondOutput.getCapturedRow());
        assertEquals(3, secondOutput.getCapturedCol());
        assertFalse(secondOutput.isHasMoreTakes());
        assertEquals("black", gameService.getGame(gameId).getCurrentPlayer());
    }

    @Test
    void multipleCapturesInOneTurn_ByWhiteKing() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();
        Piece[][] board = gameState.getBoard();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board[r][c] = null;
            }
        }

        Piece whiteKing = new Piece(PieceColor.WHITE, PieceType.KING);
        board[4][4] = whiteKing;

        board[5][3] = new Piece(PieceColor.BLACK, PieceType.PAWN);
        board[5][1] = new Piece(PieceColor.BLACK, PieceType.PAWN);

        MoveInput firstCapture = new MoveInput(4, 4, 6, 2);
        MoveOutput firstOutput = gameService.makeMove(gameId, firstCapture);

        assertNotNull(firstOutput);
        assertTrue(firstOutput.isCaptured());
        assertEquals(5, firstOutput.getCapturedRow());
        assertEquals(3, firstOutput.getCapturedCol());
        assertTrue(firstOutput.isHasMoreTakes());

        board[5][3] = null;
        board[4][4] = null;

        MoveInput secondCapture = new MoveInput(6, 2, 4, 0);
        MoveOutput secondOutput = gameService.makeMove(gameId, secondCapture);

        assertNotNull(secondOutput);
        assertTrue(secondOutput.isCaptured());

        if (!secondOutput.isHasMoreTakes()) {
            assertEquals("black", gameService.getGame(gameId).getCurrentPlayer());
        }
    }

    @Test
    void shouldEndGame_WhenAllBlackPiecesAreCaptured() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();
        Piece[][] board = gameState.getBoard();

        for (Piece[] pieces : board) {
            Arrays.fill(pieces, null);
        }

        board[3][2] = new Piece(PieceColor.BLACK, PieceType.PAWN);
        board[4][1] = new Piece(PieceColor.WHITE, PieceType.PAWN);

        MoveInput captureMove = new MoveInput(4, 1, 2, 3);
        MoveOutput result = gameService.makeMove(gameId, captureMove);

        assertNotNull(result);
        assertTrue(result.isCaptured());
        assertFalse(result.isHasMoreTakes());
        assertTrue(gameState.isFinished());
        assertEquals("white", gameState.getWinner());
    }
}
