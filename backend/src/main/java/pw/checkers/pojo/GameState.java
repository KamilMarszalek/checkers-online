package pw.checkers.pojo;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GameState {
    private String gameId;
    private Piece[][] board;
    private String currentPlayer;
    private boolean isFinished;
    private String winner;
    private int whitePiecesLeft;
    private int blackPiecesLeft;
    private int noCapturesCounter;
    private Map<String, Integer> numberOfPositions;

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
