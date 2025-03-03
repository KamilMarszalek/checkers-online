package pw.checkers.message;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MoveInput extends Message {
    private String gameId;
    private Move move;
}
