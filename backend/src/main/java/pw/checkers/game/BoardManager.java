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
            int[] capturedPieceCoordinates = getCapturedPieceCoordinates(move);
            updateCountersAfterCapture(gameState, capturedPiece);
            board[capturedPieceCoordinates[0]][capturedPieceCoordinates[1]] = null;
            setMoveOutput(moveOutput, capturedPieceCoordinates[0], capturedPieceCoordinates[1]);
        }
    }

    private int[] getCapturedPieceCoordinates(Move move) {
        int opponentRow = (move.getToRow() +move.getFromRow()) / 2;
        int opponentCol = (move.getToCol() + move.getFromCol()) / 2;
        return new int[]{opponentRow, opponentCol};
    }

    private void updateCountersAfterCapture(GameState gameState, Piece capturedPiece) {
        if (capturedPiece.getColor().equals(Color.BLACK)) {
            gameState.setBlackPiecesLeft(gameState.getBlackPiecesLeft() - 1);
        } else if (capturedPiece.getColor().equals(Color.WHITE)) {
            gameState.setWhitePiecesLeft(gameState.getWhitePiecesLeft() - 1);
        }
        gameState.setNoCapturesCounter(0);
    }

    private Piece getCapturedPiece(Move move, Piece[][] board) {
        int[] pieceCoordinates = getCapturedPieceCoordinates(move);
        return board[pieceCoordinates[0]][pieceCoordinates[1]];
    }

    private void setMoveOutput(MoveOutput moveOutput, int row, int col) {
        moveOutput.setCapturedPiece(new MoveHelper(row, col));
        moveOutput.setCaptured(true);
    }

    private boolean isCaptureMove(Move move) {
        return abs(move.getFromCol() - move.getToCol()) > 1 && abs(move.getFromRow() - move.getToRow()) > 1;
    }

    private void promotePiece(Piece pawn, Move move, GameState gameState) {
        if ((gameState.getCurrentPlayer().equals(Color.WHITE) && move.getToRow() == 0) || (gameState.getCurrentPlayer().equals(Color.BLACK) && move.getToRow() == 7)) {
            pawn.setType(PieceType.KING);
        }
    }

    private void movePiece(GameState gameState, Move move) {
        Piece[][] board = gameState.getBoard();
        Piece piece = board[move.getFromRow()][move.getFromCol()];
        board[move.getToRow()][move.getToCol()] = piece;
        board[move.getFromRow()][move.getFromCol()] = null;
    }

    private void incrementNoCapturesCounter(GameState gameState) {
        gameState.setNoCapturesCounter(gameState.getNoCapturesCounter() + 1);
    }

    private void updatePositionsCounter(GameState gameState) {
        String boardString = gameState.boardToString();
        int posCounter = gameState.getNumberOfPositions().getOrDefault(boardString, 0);
        gameState.getNumberOfPositions().put(boardString, posCounter + 1);
    }

    private boolean handleAdditionalTakes(GameState gameState, Move move, MoveOutput response) {
        if (gameRules.hasMoreTakes(gameState, move)) {
            response.setHasMoreTakes(true);
            String currentPlayer = gameState.getCurrentPlayer().toString().toLowerCase();
            response.setCurrentTurn(currentPlayer);
            response.setPreviousTurn(currentPlayer);
            gameState.setLastCaptureCol(move.getToCol());
            gameState.setLastCaptureRow(move.getToRow());
            return true;
        } else {
            gameState.setLastCaptureCol(null);
            gameState.setLastCaptureRow(null);
            return false;
        }
    }

    private void handleGameEnd(GameState gameState) {
        if (gameRules.hasSomebodyWon(gameState)) {
            gameEndManager.setWinner(gameState);
        } else if (gameRules.isDraw(gameState)) {
            gameEndManager.setDraw(gameState);
        }
    }

    private void switchPlayer(GameState gameState, MoveOutput response) {
        if (gameState.getCurrentPlayer().equals(Color.WHITE)) {
            gameState.setCurrentPlayer(Color.BLACK);
            response.setCurrentTurn(Color.BLACK.getValue());
            response.setPreviousTurn(Color.WHITE.getValue());
        } else {
            gameState.setCurrentPlayer(Color.WHITE);
            response.setCurrentTurn(Color.WHITE.getValue());
            response.setPreviousTurn(Color.BLACK.getValue());
        }
    }

    public MoveOutput makeMove(GameState gameState, MoveOutput response) {
        Move move = response.getMove();
        movePiece(gameState, move);
        incrementNoCapturesCounter(gameState);
        Piece pawn = gameState.getBoard()[move.getToRow()][move.getToCol()];
        promotePiece(pawn, move, gameState);
        doTake(gameState, response);
        updatePositionsCounter(gameState);
        if (handleAdditionalTakes(gameState, move, response)) return response;
        handleGameEnd(gameState);
        switchPlayer(gameState, response);
        return response;
    }
}
