package pw.checkers.pojo;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
