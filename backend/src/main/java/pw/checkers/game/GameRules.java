package pw.checkers.game;

import org.springframework.stereotype.Service;
import pw.checkers.data.GameState;
import pw.checkers.data.Piece;
import pw.checkers.data.enums.Color;
import pw.checkers.data.enums.PieceType;
import pw.checkers.message.Move;
import pw.checkers.message.MoveHelper;
import pw.checkers.message.PossibleMoves;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;
import static pw.checkers.utils.Constants.*;
import static pw.checkers.utils.Constants.BOARD_SIZE;

@Service
public class GameRules {
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

    private PossibleMoves getPossibleMovesHelper(GameState gameState, int row, int col, boolean isKing) {
        PossibleMoves possibleMoves = new PossibleMoves();
        possibleMoves.setMoves(new ArrayList<>());
        Piece[][] board = gameState.getBoard();
        Piece piece = board[row][col];
        if (piece == null) {
            return possibleMoves;
        }
        Color color = piece.getColor();
        boolean anyCaptureInColor = hasAnyCapture(gameState, color);
        if (anyCaptureInColor) {
            findTakes(possibleMoves, board, row, col, isKing);
        } else {
            findOtherMoves(possibleMoves, board, row, col, isKing);
        }
        return possibleMoves;
    }

    private boolean hasAnyCapture(GameState gameState, Color color) {
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

    private boolean findTakes(PossibleMoves possibleMoves, Piece[][] board, int row, int col, boolean isKing) {
        Piece pawn = board[row][col];
        Color color = pawn.getColor();

        Color opponentColor = (color == Color.BLACK) ? Color.WHITE : Color.BLACK;
        List<int[]> directions = (color == Color.BLACK)
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

    private void findOtherMoves(PossibleMoves possibleMoves, Piece[][] board, int row, int col, boolean isKing) {
        Piece pawn = board[row][col];
        Color color = pawn.getColor();
        List<int[]> directions = (color == Color.BLACK)
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

    private boolean isPositionRepeatedThreeTimes(GameState gameState) {
        return gameState.getNumberOfPositions().get(gameState.boardToString()) >= 3;
    }

    private boolean isFiftyMoveViolation(GameState gameState) {
        return gameState.getNoCapturesCounter() >= 50;
    }

    public boolean hasSomebodyWon(GameState gameState) {
        Color currentPlayer = gameState.getCurrentPlayer();
        Color otherPlayer = Color.WHITE.equals(currentPlayer) ? Color.BLACK : Color.WHITE;

        return gameState.getBlackPiecesLeft() == 0
                || gameState.getWhitePiecesLeft() == 0
                || (playerHasMoves(gameState, otherPlayer) && !playerHasMoves(gameState, currentPlayer));
    }

    public boolean playerHasMoves(GameState gameState, Color player) {
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

    public boolean hasMoreTakes(GameState gameState, Move move) {
        Piece[][] board = gameState.getBoard();
        boolean isKing = board[move.getToRow()][move.getToCol()].getType().equals(PieceType.KING);
        if (abs(move.getFromCol() - move.getToCol()) > 1 && abs(move.getFromRow() - move.getToRow()) > 1 ) {
            return findTakes(new PossibleMoves(), board, move.getToRow(), move.getToCol(), isKing);
        }
        return false;
    }

    public boolean isDraw(GameState gameState) {
        Color currentPlayer = gameState.getCurrentPlayer();
        Color otherPlayer = currentPlayer.equals(Color.WHITE) ? Color.BLACK : Color.WHITE;

        return (!playerHasMoves(gameState, currentPlayer) && !playerHasMoves(gameState, otherPlayer))
                || isFiftyMoveViolation(gameState)
                || isPositionRepeatedThreeTimes(gameState);
    }
}
