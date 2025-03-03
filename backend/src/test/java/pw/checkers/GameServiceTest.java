package pw.checkers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import pw.checkers.data.GameState;
import pw.checkers.data.Piece;
import pw.checkers.data.enums.Color;
import pw.checkers.data.enums.PieceType;
import pw.checkers.game.*;
import pw.checkers.message.*;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    @Spy
    private GameRules gameRules = new GameRules();

    @Spy
    private GameEndManager gameEndManager = new GameEndManager(gameRules);

    @Spy
    private BoardManager boardManager = new BoardManager(gameEndManager, gameRules);

    @Spy
    private MoveValidator moveValidator = new MoveValidator(boardManager, gameRules);

    @InjectMocks
    private GameServiceImpl gameService;

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
        assertEquals(Color.WHITE, gameState.getCurrentPlayer());
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
                        assertEquals(Color.BLACK, piece.getColor());
                        assertEquals(PieceType.PAWN, piece.getType());
                    } else {
                        assertNull(piece);
                    }
                } else if (row > 4) {
                    if ((row + col) % 2 == 1) {
                        assertNotNull(piece);
                        assertEquals(Color.WHITE, piece.getColor());
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
    void deleteGame_ShouldDeleteExistingGame(){
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();

        gameService.deleteGame(gameId);
        GameState fetchedGame = gameService.getGame(gameId);

        assertNull(fetchedGame);
    }

    @Test
    void deleteGame_ShouldHandleNonExistingGame() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();

        gameService.deleteGame("random");
        GameState fetchedGame = gameService.getGame(gameId);
        assertNull(gameService.getGame("random"));
        assertNotNull(fetchedGame);
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

        Move invalidMove = new Move(0, 0, 1, 1);
        MoveOutput result = gameService.makeMove(gameId, invalidMove, "white");

        assertNull(result);
    }

    @Test
    void makeMove_ShouldChangeTurnWhenMoveIsValid() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();

        Move validMove = new Move(5, 2, 4, 3);
        MoveOutput result = gameService.makeMove(gameId, validMove, "white");

        assertNotNull(result);
        assertFalse(result.isCaptured());
        assertEquals(result.getMove().getFromCol(), validMove.getFromCol());
        assertEquals(result.getMove().getFromRow(), validMove.getFromRow());
        assertEquals(result.getMove().getToCol(), validMove.getToCol());
        assertEquals(result.getMove().getToRow(), validMove.getToRow());
        assertNull(result.getCapturedPiece());
        assertEquals(result.getCurrentTurn(), "black");
        assertEquals(result.getPreviousTurn(), "white");
    }

    @Test
    void makeMove_ShouldHandleSingleCaptureCorrectly() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();
        Piece[][] board = gameState.getBoard();

        board[4][3] = new Piece(Color.BLACK, PieceType.PAWN);
        board[5][2] = new Piece(Color.WHITE, PieceType.PAWN);

        Move captureMove = new Move(5, 2, 3, 4);
        MoveOutput result = gameService.makeMove(gameId, captureMove, "white");

        assertNotNull(result);
        assertEquals(result.getMove().getFromCol(), captureMove.getFromCol());
        assertEquals(result.getMove().getFromRow(), captureMove.getFromRow());
        assertEquals(result.getMove().getToCol(), captureMove.getToCol());
        assertEquals(result.getMove().getToRow(), captureMove.getToRow());
        assertEquals(result.getCapturedPiece().getRow(), 4);
        assertEquals(result.getCapturedPiece().getCol(), 3);
        assertFalse(result.isHasMoreTakes());
        assertTrue(result.isCaptured());
        assertNull(board[4][3]);
        assertEquals(result.getPreviousTurn(), "white");
        assertEquals(result.getCurrentTurn(), "black");
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

        board[4][3] = new Piece(Color.BLACK, PieceType.PAWN);
        board[2][3] = new Piece(Color.BLACK, PieceType.PAWN);
        board[5][2] = new Piece(Color.WHITE, PieceType.PAWN);

        Move captureMove = new Move(5, 2, 3, 4);
        MoveOutput result = gameService.makeMove(gameId, captureMove, "white");

        assertNotNull(result);
        assertEquals(result.getMove().getFromCol(), captureMove.getFromCol());
        assertEquals(result.getMove().getFromRow(), captureMove.getFromRow());
        assertEquals(result.getMove().getToCol(), captureMove.getToCol());
        assertEquals(result.getMove().getToRow(), captureMove.getToRow());
        assertEquals(result.getCapturedPiece().getRow(), 4);
        assertEquals(result.getCapturedPiece().getCol(), 3);
        assertTrue(result.isHasMoreTakes());
        assertTrue(result.isCaptured());
        assertNull(board[4][3]);
        assertEquals(result.getPreviousTurn(), "white");
        assertEquals(result.getCurrentTurn(), "white");
    }

    @Test
    void makeMove_ShouldPromotePawnToKing() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();
        Piece[][] board = gameState.getBoard();
        board[0][3] = null;

        board[1][2] = new Piece(Color.WHITE, PieceType.PAWN);
        Move promotionMove = new Move(1, 2, 0, 3);

        gameService.makeMove(gameId, promotionMove, "white");

        assertEquals(PieceType.KING, board[0][3].getType());
    }

    @Test
    void makeMove_ShouldReturnNull_WhenMovingPieceOfWrongColor() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();

        Move blackMove = new Move(2, 1, 3, 2);
        MoveOutput result = gameService.makeMove(gameId, blackMove, "white");

        assertNull(result);
    }

    @Test
    void makeMove_ShouldReturnNull_WhenFromFieldIsEmpty() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();

        Move emptyFieldMove = new Move(4, 4, 3, 3);
        MoveOutput result = gameService.makeMove(gameId, emptyFieldMove, "white");

        assertNull(result);
    }

    @Test
    void makeMove_ShouldReturnNull_WhenGameIsAlreadyFinished() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();
        gameState.setFinished(true);

        Move anyMove = new Move(5, 2, 4, 3);
        MoveOutput result = gameService.makeMove(gameId, anyMove, "white");

        assertNull(result);
    }

    @Test
    void getPossibleMoves_ShouldReturnOnlyCaptureMoves_IfCaptureExists() {
        GameState gameState = gameService.createGame();
        Piece[][] board = gameState.getBoard();

        for (Piece[] pieces : board) {
            Arrays.fill(pieces, null);
        }
        board[4][3] = new Piece(Color.BLACK, PieceType.PAWN);
        board[5][2] = new Piece(Color.WHITE, PieceType.PAWN);


        PossibilitiesOutput pm = gameService.getPossibleMoves(gameState, 5, 2);

        assertEquals(1, pm.getMoves().size());
        MoveHelper move = pm.getMoves().getFirst();
        assertEquals(3, move.getRow());
        assertEquals(4, move.getCol());
    }

    @Test
    void getPossibleMoves_ShouldReturnAllDirectionsForKing() {
        GameState gameState = gameService.createGame();
        Piece[][] board = gameState.getBoard();

        for (Piece[] pieces : board) {
            Arrays.fill(pieces, null);
        }
        board[4][4] = new Piece(Color.WHITE, PieceType.KING);

        PossibilitiesOutput pm = gameService.getPossibleMoves(gameState, 4, 4);

        assertEquals(4, pm.getMoves().size(), "King in the middle should have 4 possible moves if no captures exist");

        boolean hasUpLeft = pm.getMoves().stream()
                .anyMatch(m -> m.getRow() == 3 && m.getCol() == 3);
        boolean hasUpRight = pm.getMoves().stream()
                .anyMatch(m -> m.getRow() == 3 && m.getCol() == 5);
        boolean hasDownLeft = pm.getMoves().stream()
                .anyMatch(m -> m.getRow() == 5 && m.getCol() == 3);
        boolean hasDownRight = pm.getMoves().stream()
                .anyMatch(m -> m.getRow() == 5 && m.getCol() == 5);

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

        board[5][0] = new Piece(Color.WHITE, PieceType.PAWN);
        board[3][4] = new Piece(Color.WHITE, PieceType.PAWN);

        board[4][1] = new Piece(Color.BLACK, PieceType.PAWN);
        board[2][3] = new Piece(Color.BLACK, PieceType.PAWN);

        Move firstCapture = new Move(5, 0, 3, 2);
        MoveOutput firstOutput = gameService.makeMove(gameId, firstCapture,"white");

        Move invalidCapture = new Move(3, 4, 1, 2);
        MoveOutput invalidOutput = gameService.makeMove(gameId, invalidCapture,"white");

        assertNull(invalidOutput);

        assertNotNull(firstOutput);
        assertTrue(firstOutput.isCaptured());
        assertEquals(4, firstOutput.getCapturedPiece().getRow());
        assertEquals(1, firstOutput.getCapturedPiece().getCol());
        assertTrue(firstOutput.isHasMoreTakes());
        assertEquals(firstOutput.getCurrentTurn(), "white");
        assertEquals(firstOutput.getPreviousTurn(), "white");

        Move secondCapture = new Move(3, 2, 1, 4);
        MoveOutput secondOutput = gameService.makeMove(gameId, secondCapture, "white");

        assertNotNull(secondOutput);
        assertTrue(secondOutput.isCaptured());
        assertEquals(2, secondOutput.getCapturedPiece().getRow());
        assertEquals(3, secondOutput.getCapturedPiece().getCol());
        assertFalse(secondOutput.isHasMoreTakes());
        assertEquals("black", secondOutput.getCurrentTurn());
        assertEquals("white", secondOutput.getPreviousTurn());
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

        Piece whiteKing = new Piece(Color.WHITE, PieceType.KING);
        board[4][4] = whiteKing;

        board[5][3] = new Piece(Color.BLACK, PieceType.PAWN);
        board[5][1] = new Piece(Color.BLACK, PieceType.PAWN);

        Move firstCapture = new Move(4, 4, 6, 2);
        MoveOutput firstOutput = gameService.makeMove(gameId, firstCapture, "white");

        assertNotNull(firstOutput);
        assertTrue(firstOutput.isCaptured());
        assertEquals(5, firstOutput.getCapturedPiece().getRow());
        assertEquals(3, firstOutput.getCapturedPiece().getCol());
        assertTrue(firstOutput.isHasMoreTakes());

        board[5][3] = null;
        board[4][4] = null;

        Move secondCapture = new Move(6, 2, 4, 0);
        MoveOutput secondOutput = gameService.makeMove(gameId, secondCapture, "white");

        assertNotNull(secondOutput);
        assertTrue(secondOutput.isCaptured());

        if (!secondOutput.isHasMoreTakes()) {
            assertEquals(Color.BLACK, gameService.getGame(gameId).getCurrentPlayer());
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

        board[3][2] = new Piece(Color.BLACK, PieceType.PAWN);
        board[4][1] = new Piece(Color.WHITE, PieceType.PAWN);

        gameState.setBlackPiecesLeft(1);
        gameState.setWhitePiecesLeft(1);

        Move captureMove = new Move(4, 1, 2, 3);
        MoveOutput result = gameService.makeMove(gameId, captureMove, "white");

        assertNotNull(result);
        assertTrue(result.isCaptured());
        assertFalse(result.isHasMoreTakes());
        assertTrue(gameState.isFinished());
        assertEquals(Color.WHITE, gameState.getWinner());
    }

    @Test
    void testNoPossibleMovesForEitherSide_ShouldResultInDraw() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();
        Piece[][] board = gameState.getBoard();

        for (Piece[] pieces : board) {
            Arrays.fill(pieces, null);
        }
        board[0][0] = new Piece(Color.WHITE, PieceType.PAWN);
        board[0][2] = new Piece(Color.WHITE, PieceType.PAWN);
        board[7][5] = new Piece(Color.BLACK, PieceType.PAWN);
        board[7][7] = new Piece(Color.BLACK, PieceType.PAWN);
        board[2][2] = new Piece(Color.WHITE, PieceType.PAWN);
        gameService.makeMove(gameId, new Move(2,2,1,1), "white");
        boolean isDraw = (gameState.isFinished() && gameState.getWinner() == null);
        assertTrue(isDraw, "Game should be finished as a draw if no moves are possible for both sides.");
    }

    @Test
    void testFiftyMovesWithoutCapture_ShouldResultInDraw() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();
        gameState.setNoCapturesCounter(49);
        Move move = new Move(5, 2, 4, 3);
        MoveOutput moveResult = gameService.makeMove(gameId, move, "white");

        assertNotNull(moveResult, "Move itself should be valid (assuming it was indeed valid on board).");
        boolean isDraw = (gameState.isFinished() && gameState.getWinner() == null);

        assertTrue(isDraw, "Game should be finished as a draw after 50 moves without capture.");
    }

    @Test
    void testThreefoldRepetition_ShouldResultInDraw() {
        GameState gameState = gameService.createGame();
        String gameId = gameState.getGameId();
        Piece[][] board = gameState.getBoard();

        for (Piece[] pieces : board) {
            Arrays.fill(pieces, null);
        }

        board[0][2] = new Piece(Color.WHITE, PieceType.KING);
        board[7][5] = new Piece(Color.BLACK, PieceType.KING);

        gameService.makeMove(gameId, new Move(0, 2, 1, 3), "white");
        gameService.makeMove(gameId, new Move(7, 5, 6, 4), "black");
        gameService.makeMove(gameId, new Move(1, 3, 0, 2), "white");
        gameService.makeMove(gameId, new Move(6, 4, 7, 5), "black");
        gameService.makeMove(gameId, new Move(0, 2, 1, 3), "white");
        gameService.makeMove(gameId, new Move(7, 5, 6, 4), "black");
        gameService.makeMove(gameId, new Move(1, 3, 0, 2), "white");
        gameService.makeMove(gameId, new Move(6, 4, 7, 5), "black");
        gameService.makeMove(gameId, new Move(0, 2, 1, 3), "white");
        gameService.makeMove(gameId, new Move(7, 5, 6, 4), "black");
        gameService.makeMove(gameId, new Move(1, 3, 0, 2), "white");
        gameService.makeMove(gameId, new Move(6, 4, 7, 5), "black");


        boolean isDraw = (gameState.isFinished() && gameState.getWinner() == null);
        assertTrue(isDraw);
    }
}
