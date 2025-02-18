package pw.checkers.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MoveOutput {
    private Move move;
    private boolean captured;
    private MoveHelper capturedPiece;
    private boolean hasMoreTakes;
    private String currentTurn;
    private String previousTurn;
}
