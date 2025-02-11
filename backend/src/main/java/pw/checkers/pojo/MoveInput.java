package pw.checkers.pojo;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class MoveInput {
    private String gameId;
    private Move move;
}
