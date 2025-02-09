package pw.checkers.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MoveOutput extends MoveInput{
    private boolean captured;
    private Integer capturedRow;
    private Integer capturedCol;
    private boolean hasMoreTakes;
    private String turn;

    public MoveOutput (MoveInput move) {
        super(move.getFromRow(), move.getFromCol(), move.getToRow(), move.getToCol());
    }
}
