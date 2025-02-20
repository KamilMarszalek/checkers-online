package pw.checkers.data;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pw.checkers.data.enums.PieceColor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GameState {
    private String gameId;
    private Piece[][] board;
    private PieceColor currentPlayer;
    private boolean isFinished;
    private PieceColor winner;
    private int whitePiecesLeft;
    private int blackPiecesLeft;
    private int noCapturesCounter;
    private Map<String, Integer> numberOfPositions;
    private Integer lastCaptureCol;
    private Integer lastCaptureRow;

    public String boardToString() {
        StringBuilder response = new StringBuilder();
        for (Piece[] row : board) {
            for (Piece piece : row) {
                if (piece == null) {
                    response.append(".");
                } else {
                    response.append(piece.toString());
                }
            }
        }
        response.append(currentPlayer);
        return response.toString();
    }
}
