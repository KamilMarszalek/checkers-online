package pw.checkers.game;

import org.springframework.stereotype.Service;
import pw.checkers.data.GameState;
import pw.checkers.data.Piece;
import pw.checkers.data.enums.Color;
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
                board[row][col] = new Piece(Color.BLACK, PieceType.PAWN);
            }
        }
        for (int row = 5; row < BOARD_SIZE; row++) {
            for (int col = (row + 1) % 2; col < BOARD_SIZE; col+=2) {
                board[row][col] = new Piece(Color.WHITE, PieceType.PAWN);
            }
        }
        gameState.setBoard(board);
    }

    private void doTake(GameState gameState, MoveOutput moveOutput) {
        Move move = moveOutput.getMove();
        Piece[][] board = gameState.getBoard();
        if (isCaptureMove(move)) {
            Piece capturedPiece = getCapturedPiece(move, board);
            int[] capturedPieceCoordinates = getCapturedPieceCoordinates(move, board);
            updateCounters(gameState, capturedPiece);
            board[capturedPieceCoordinates[0]][capturedPieceCoordinates[1]] = null;
            moveOutput.setCapturedPiece(new MoveHelper(capturedPieceCoordinates[0], capturedPieceCoordinates[1]));
            moveOutput.setCaptured(true);
        }
    }

    private int[] getCapturedPieceCoordinates(Move move, Piece[][] board) {
        int opponentRow = (move.getToRow() +move.getFromRow()) / 2;
        int opponentCol = (move.getToCol() + move.getFromCol()) / 2;
        return new int[]{opponentRow, opponentCol};
    }

    private void updateCounters(GameState gameState, Piece capturedPiece) {
        if (capturedPiece.getColor().equals(Color.BLACK)) {
            gameState.setBlackPiecesLeft(gameState.getBlackPiecesLeft() - 1);
        } else if (capturedPiece.getColor().equals(Color.WHITE)) {
            gameState.setWhitePiecesLeft(gameState.getWhitePiecesLeft() - 1);
        }
        gameState.setNoCapturesCounter(0);
    }

    private Piece getCapturedPiece(Move move, Piece[][] board) {
        int[] pieceCoordinates = getCapturedPieceCoordinates(move, board);
        return board[pieceCoordinates[0]][pieceCoordinates[1]];
    }

    private void setMoveOutput(MoveOutput moveOutput, int row, int col) {
        moveOutput.setCapturedPiece(new MoveHelper(row, col));
    }

    private boolean isCaptureMove(Move move) {
        return abs(move.getFromCol() - move.getToCol()) > 1 && abs(move.getFromRow() - move.getToRow()) > 1;
    }

    private void promotePiece(Piece pawn, Move move, GameState gameState) {
        if ((gameState.getCurrentPlayer().equals(Color.WHITE) && move.getToRow() == 0) || (gameState.getCurrentPlayer().equals(Color.BLACK) && move.getToRow() == 7)) {
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

        if (gameState.getCurrentPlayer().equals(Color.WHITE)) {
            gameState.setCurrentPlayer(Color.BLACK);
            response.setCurrentTurn(Color.BLACK.getValue());
            response.setPreviousTurn(Color.WHITE.getValue());
        } else {
            gameState.setCurrentPlayer(Color.WHITE);
            response.setCurrentTurn(Color.WHITE.getValue());
            response.setPreviousTurn(Color.BLACK.getValue());
        }
        return response;
    }
}
