package pw.checkers.game;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pw.checkers.data.GameState;
import pw.checkers.data.Piece;
import pw.checkers.data.enums.PieceColor;
import pw.checkers.message.Move;
import pw.checkers.message.MoveHelper;
import pw.checkers.message.PossibleMoves;

@NoArgsConstructor
@Getter
@Setter
public class MoveValidator {
    private final BoardManager boardManager = new BoardManager();

    public boolean validateMove(GameState gameState, Move move) {
        if (isGameStateNull(gameState)) {
            return false;
        }

        if (isGameFinished(gameState)) {
            return false;
        }

        Piece[][] board = gameState.getBoard();
        Piece piece = board[move.getFromRow()][move.getFromCol()];

        if (isPieceNull(piece)) {
            return false;
        }

        if (isPieceWrongColor(piece, gameState.getCurrentPlayer())) {
            return false;
        }

        if (brokenMultipleCaptureSequence(move, gameState)) {
            return false;
        }
        PossibleMoves possibleMoves = boardManager.getPossibleMoves(gameState, move.getFromRow(), move.getFromCol());
        return possibleMoves.getMoves().contains(new MoveHelper(move.getToRow(), move.getToCol()));

    }

    private boolean brokenMultipleCaptureSequence(Move move, GameState gameState) {
        if (gameState.getLastCaptureCol() != null && gameState.getLastCaptureRow() != null) {
            return  (move.getFromRow() != gameState.getLastCaptureRow() || move.getFromCol() != gameState.getLastCaptureCol());
        }
        return false;
    }

    private boolean isPieceWrongColor(Piece piece, PieceColor currentPlayer) {
        return !piece.getColor().equals(currentPlayer);
    }

    private boolean isPieceNull(Piece piece) {
        return piece == null;
    }

    private boolean isGameStateNull(GameState gameState) {
        return gameState == null;
    }

    private boolean isGameFinished(GameState gameState) {
        return gameState.isFinished();
    }
}
