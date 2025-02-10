package pw.checkers.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MoveOutput {
    private MoveInput move;
    private boolean captured;
    private Integer capturedRow;
    private Integer capturedCol;
    private boolean hasMoreTakes;
    private String turn;
}
