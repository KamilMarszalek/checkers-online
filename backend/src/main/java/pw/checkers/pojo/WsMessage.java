package pw.checkers.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WsMessage {
    private String type;
    private String gameId;
    private MoveInput move;
    private int row;
    private int col;
}
