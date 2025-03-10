package pw.checkers.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pw.checkers.data.enums.MessageType;

@Getter
@Setter
@NoArgsConstructor
public class GameEndMessage extends Message {
    private String result;
    private String details;

    public GameEndMessage(String result) {
        super(MessageType.GAME_END.getValue());
        this.result = result;
    }
}
