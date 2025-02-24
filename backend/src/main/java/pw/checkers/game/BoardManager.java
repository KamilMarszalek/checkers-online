package pw.checkers.game;

import org.springframework.stereotype.Service;
import pw.checkers.data.GameState;
import pw.checkers.data.Piece;
import pw.checkers.data.enums.PieceColor;
import pw.checkers.data.enums.PieceType;
import pw.checkers.message.Move;
import pw.checkers.message.MoveHelper;
import pw.checkers.message.MoveOutput;

import static java.lang.Math.abs;
import static pw.checkers.utils.Constants.*;

@Service
public class BoardManager {
    private final GameEndManager gameEndManager;
    private final GameRules gameRules;

    public BoardManager(GameEndManager gameEndManager, GameRules gameRules) {
        this.gameEndManager = gameEndManager;
        this.gameRules = gameRules;
    }

    public void initializeBoard(GameState gameState){
        Piece[][] board = new Piece[BOARD_SIZE][BOARD_SIZE];
        for (int row = 0; row < 3; row++) {
            for (int col = (row + 1) % 2; col < BOARD_SIZE; col+=2) {
                board[row][col] = new Piece(PieceColor.BLACK, PieceType.PAWN);
            }
        }
        for (int row = 5; row < BOARD_SIZE; row++) {
            for (int col = (row + 1) % 2; col < BOARD_SIZE; col+=2) {
                board[row][col] = new Piece(PieceColor.WHITE, PieceType.PAWN);
            }
        }
        gameState.setBoard(board);
    }

    private void doTake(GameState gameState, MoveOutput move) {
        Piece[][] board = gameState.getBoard();
        if (abs(move.getMove().getFromCol() - move.getMove().getToCol()) > 1 && abs(move.getMove().getFromRow() - move.getMove().getToRow()) > 1) {
            int opponentRow = (move.getMove().getToRow() + move.getMove().getFromRow()) / 2;
            int opponentCol = (move.getMove().getToCol() + move.getMove().getFromCol()) / 2;
            Piece capturedPiece = board[opponentRow][opponentCol];
            if (capturedPiece.getColor().equals(PieceColor.BLACK)) {
                gameState.setBlackPiecesLeft(gameState.getBlackPiecesLeft() - 1);
            } else if (capturedPiece.getColor().equals(PieceColor.WHITE)) {
                gameState.setWhitePiecesLeft(gameState.getWhitePiecesLeft() - 1);
            }
            board[opponentRow][opponentCol] = null;
            move.setCapturedPiece(new MoveHelper(opponentRow, opponentCol));
            move.setCaptured(true);
            gameState.setNoCapturesCounter(0);
        }
    }

    private void promotePiece(Piece pawn, Move move, GameState gameState) {
        if ((gameState.getCurrentPlayer().equals(PieceColor.WHITE) && move.getToRow() == 0) || (gameState.getCurrentPlayer().equals(PieceColor.BLACK) && move.getToRow() == 7)) {
            pawn.setType(PieceType.KING);
        }
    }


    public MoveOutput makeMove(GameState gameState, MoveOutput response) {
        Move move = response.getMove();
        Piece[][] board = gameState.getBoard();
        Piece pawn = board[move.getFromRow()][move.getFromCol()];
        board[move.getToRow()][move.getToCol()] = pawn;
        board[move.getFromRow()][move.getFromCol()] = null;
        gameState.setNoCapturesCounter(gameState.getNoCapturesCounter() + 1);
        promotePiece(pawn, move, gameState);
        doTake(gameState, response);
        int posCounter = gameState.getNumberOfPositions().get(gameState.boardToString()) == null ? 0 : gameState.getNumberOfPositions().get(gameState.boardToString());
        gameState.getNumberOfPositions().put(gameState.boardToString(), posCounter + 1);
        if (gameRules.hasMoreTakes(gameState, move)) {
            response.setHasMoreTakes(true);
            response.setCurrentTurn(gameState.getCurrentPlayer().toString().toLowerCase());
            response.setPreviousTurn(gameState.getCurrentPlayer().toString().toLowerCase());
            gameState.setLastCaptureCol(move.getToCol());
            gameState.setLastCaptureRow(move.getToRow());
            return response;
        } else {
            gameState.setLastCaptureCol(null);
            gameState.setLastCaptureRow(null);
        }
        if (gameRules.hasSomebodyWon(gameState)) {
            gameEndManager.setWinner(gameState);
        } else if (gameRules.isDraw(gameState)) {
            gameEndManager.setDraw(gameState);
        }

        if (gameState.getCurrentPlayer().equals(PieceColor.WHITE)) {
            gameState.setCurrentPlayer(PieceColor.BLACK);
            response.setCurrentTurn("black");
            response.setPreviousTurn("white");
        } else {
            gameState.setCurrentPlayer(PieceColor.WHITE);
            response.setCurrentTurn("white");
            response.setPreviousTurn("black");
        }
        return response;
    }
}
