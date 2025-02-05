package pw.checkers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import pw.checkers.pojo.*;
import pw.checkers.service.GameService;

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
        assertEquals("black", gameService.getGame(gameId).getCurrentPlayer());
    }

    @Test
    void makeMove_ShouldHandleCaptureCorrectly() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();
        Piece[][] board = gameState.getBoard();

        board[4][3] = new Piece(PieceColor.BLACK, PieceType.PAWN);
        board[5][2] = new Piece(PieceColor.WHITE, PieceType.PAWN);

        MoveInput captureMove = new MoveInput(5, 2, 3, 4);
        MoveOutput result = gameService.makeMove(gameId, captureMove);

        assertNotNull(result);
        assertTrue(result.isCaptured());
        assertNull(board[4][3]);
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
}
