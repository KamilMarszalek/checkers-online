package pw.checkers.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pw.checkers.data.enums.MessageType;

@Getter
@Setter
@NoArgsConstructor
public class GameEnd extends Message {
    private String result;
    private String details;

    public GameEnd(String result) {
        super(MessageType.GAME_END.getValue());
        this.result = result;
    }
}
