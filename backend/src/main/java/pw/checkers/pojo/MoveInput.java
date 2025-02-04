package pw.checkers.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MoveInput {
    private int fromRow;
    private int fromColumn;
    private int toRow;
    private int toColumn;
}
