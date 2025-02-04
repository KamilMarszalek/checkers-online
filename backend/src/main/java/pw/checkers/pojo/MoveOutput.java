package pw.checkers.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MoveOutput {
    MoveInput move;
    boolean captured;
    Integer capturedRow;
    Integer capturedCol;
}
