package pw.checkers.pojo;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Move {
    private int fromRow;
    private int fromCol;
    private int toRow;
    private int toCol;
}
