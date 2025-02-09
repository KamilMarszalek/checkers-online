package pw.checkers.pojo;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class MoveInput {
    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;
}
