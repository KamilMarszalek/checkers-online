package pw.checkers.game;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pw.checkers.data.GameState;
import pw.checkers.data.Piece;
import pw.checkers.data.enums.PieceColor;
import pw.checkers.data.enums.PieceType;
import pw.checkers.message.Move;
import pw.checkers.message.MoveHelper;
import pw.checkers.message.MoveOutput;
import pw.checkers.message.PossibleMoves;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;
import static pw.checkers.utils.Constants.*;
import static pw.checkers.utils.Constants.DIRECTIONS_KING;

@NoArgsConstructor
@Getter
@Setter
public class BoardManager {
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

    private boolean hasMoreTakes(GameState gameState, Move move) {
        Piece[][] board = gameState.getBoard();
        boolean isKing = board[move.getToRow()][move.getToCol()].getType().equals(PieceType.KING);
        if (abs(move.getFromCol() - move.getToCol()) > 1 && abs(move.getFromRow() - move.getToRow()) > 1 ) {
            return findTakes(new PossibleMoves(), board, move.getToRow(), move.getToCol(), isKing);
        }
        return false;
    }

    private boolean findTakes(PossibleMoves possibleMoves, Piece[][] board, int row, int col, boolean isKing) {
        Piece pawn = board[row][col];
        PieceColor color = pawn.getColor();

        PieceColor opponentColor = (color == PieceColor.BLACK) ? PieceColor.WHITE : PieceColor.BLACK;
        List<int[]> directions = (color == PieceColor.BLACK)
                ? DIRECTIONS_PAWN_BLACK
                : DIRECTIONS_PAWN_WHITE;
        if (isKing) {
            directions = DIRECTIONS_KING;
        }

        for (int[] direction : directions) {
            int deltaRow = direction[0];
            int deltaCol = direction[1];

            int middleRow = row + deltaRow;
            int middleCol = col + deltaCol;

            int landingRow = row + 2 * deltaRow;
            int landingCol = col + 2 * deltaCol;

            if (landingRow < 0 || landingRow >= BOARD_SIZE
                    || landingCol < 0 || landingCol >= BOARD_SIZE) {
                continue;
            }

            if (board[middleRow][middleCol] != null
                    && board[middleRow][middleCol].getColor() == opponentColor
                    && board[landingRow][landingCol] == null) {
                possibleMoves.getMoves().add(new MoveHelper(landingRow, landingCol));
            }
        }

        return !possibleMoves.getMoves().isEmpty();
    }

    public MoveOutput makeMove(GameState gameState, MoveOutput response, PieceColor playerColor) {
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
        if (hasMoreTakes(gameState, move)) {
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
        if (hasSomebodyWon(gameState)) {
            setWinner(gameState);
        } else if (isDraw(gameState)) {
            setDraw(gameState);
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

    private void setDraw(GameState gameState) {
        gameState.setFinished(true);
    }

    private boolean isDraw(GameState gameState) {
        PieceColor currentPlayer = gameState.getCurrentPlayer();
        PieceColor otherPlayer = currentPlayer.equals(PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;

        return (!playerHasMoves(gameState, currentPlayer) && !playerHasMoves(gameState, otherPlayer))
                || isFiftyMoveViolation(gameState)
                || isPositionRepeatedThreeTimes(gameState);
    }

    private boolean playerHasMoves(GameState gameState, PieceColor player) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = (row + 1) % 2; column < BOARD_SIZE; column+=2) {
                if (gameState.getBoard()[row][column] != null) {
                    if (gameState.getBoard()[row][column].getColor().equals(player)) {
                        if (!getPossibleMoves(gameState, row, column).getMoves().isEmpty()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isPositionRepeatedThreeTimes(GameState gameState) {
        return gameState.getNumberOfPositions().get(gameState.boardToString()) >= 3;
    }

    private boolean isFiftyMoveViolation(GameState gameState) {
        return gameState.getNoCapturesCounter() >= 50;
    }


    private boolean hasSomebodyWon(GameState gameState) {
        PieceColor currentPlayer = gameState.getCurrentPlayer();
        PieceColor otherPlayer = PieceColor.WHITE.equals(currentPlayer) ? PieceColor.BLACK : PieceColor.WHITE;

        return gameState.getBlackPiecesLeft() == 0
                || gameState.getWhitePiecesLeft() == 0
                || (!playerHasMoves(gameState, otherPlayer) && playerHasMoves(gameState, currentPlayer));
    }

    private void setWinner(GameState gameState) {
        PieceColor currentPlayer = gameState.getCurrentPlayer();
        PieceColor otherPlayer = PieceColor.WHITE.equals(currentPlayer) ? PieceColor.BLACK : PieceColor.WHITE;

        if (gameState.getWhitePiecesLeft() == 0) {
            gameState.setWinner(PieceColor.BLACK);
            gameState.setFinished(true);
            return;
        } else if (gameState.getBlackPiecesLeft() == 0) {
            gameState.setWinner(PieceColor.WHITE);
            gameState.setFinished(true);
            return;
        }
        if (!playerHasMoves(gameState, otherPlayer) && playerHasMoves(gameState, currentPlayer)) {
            gameState.setWinner(currentPlayer);
            gameState.setFinished(true);
        }
    }

    public PossibleMoves getPossibleMoves(GameState gameState, int row, int col) {
        if (gameState.getLastCaptureCol() != null && gameState.getLastCaptureRow() != null) {
            if (row != gameState.getLastCaptureRow() || col != gameState.getLastCaptureCol()) {
                return new PossibleMoves();
            }
        }
        Piece[][] board = gameState.getBoard();
        Piece pawn = board[row][col];
        if (pawn == null) {
            return new PossibleMoves();
        }
        if (pawn.getType().equals(PieceType.KING)) {
            return getPossibleMovesHelper(gameState, row, col, true);
        }
        return getPossibleMovesHelper(gameState, row, col, false);
    }

    private boolean hasAnyCapture(GameState gameState, PieceColor color) {
        Piece[][] board = gameState.getBoard();

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Piece p = board[row][col];
                if (p != null && p.getColor() == color) {
                    boolean isKing = (p.getType() == PieceType.KING);
                    PossibleMoves temp = new PossibleMoves();
                    temp.setMoves(new ArrayList<>());
                    if (findTakes(temp, board, row, col, isKing)) {
                        if (!temp.getMoves().isEmpty()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private PossibleMoves getPossibleMovesHelper(GameState gameState, int row, int col, boolean isKing) {
        PossibleMoves possibleMoves = new PossibleMoves();
        possibleMoves.setMoves(new ArrayList<>());
        Piece[][] board = gameState.getBoard();
        Piece piece = board[row][col];
        if (piece == null) {
            return possibleMoves;
        }
        PieceColor color = piece.getColor();
        boolean anyCaptureInColor = hasAnyCapture(gameState, color);
        if (anyCaptureInColor) {
            findTakes(possibleMoves, board, row, col, isKing);
        } else {
            findOtherMoves(possibleMoves, board, row, col, isKing);
        }
        return possibleMoves;
    }

    private void findOtherMoves(PossibleMoves possibleMoves,Piece[][] board, int row, int col, boolean isKing) {
        Piece pawn = board[row][col];
        PieceColor color = pawn.getColor();
        List<int[]> directions = (color == PieceColor.BLACK)
                ? DIRECTIONS_PAWN_BLACK
                : DIRECTIONS_PAWN_WHITE;
        if (isKing) {
            directions = DIRECTIONS_KING;
        }
        for (int[] direction : directions) {
            int deltaRow = direction[0];
            int deltaCol = direction[1];

            int landingRow = row + deltaRow;
            int landingCol = col + deltaCol;

            if (landingRow < 0 || landingRow >= BOARD_SIZE
                    || landingCol < 0 || landingCol >= BOARD_SIZE) {
                continue;
            }

            if (board[landingRow][landingCol] == null) {
                possibleMoves.getMoves().add(new MoveHelper(landingRow, landingCol));
            }
        }
    }
}
