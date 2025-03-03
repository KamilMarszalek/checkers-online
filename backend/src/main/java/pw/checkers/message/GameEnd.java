package pw.checkers.message;

import lombok.Getter;
import lombok.Setter;
import pw.checkers.data.enums.MessageType;

@Getter
@Setter
public class GameEnd extends Message{
    private String result;

    public GameEnd() {
        super(MessageType.GAME_END.getValue());
    }

    public GameEnd(String result) {
        super(MessageType.GAME_END.getValue());
        this.result = result;
    }
}
