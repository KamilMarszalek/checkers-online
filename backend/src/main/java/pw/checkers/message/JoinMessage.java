package pw.checkers.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pw.checkers.data.enums.MessageType;

@Getter
@Setter
@NoArgsConstructor
public class JoinMessage extends Message{
    private String gameId;
    private String color;
    private User opponent;

    public JoinMessage(String gameId, String color, User opponent) {
        super(MessageType.GAME_CREATED.getValue());
        this.gameId = gameId;
        this.color = color;
        this.opponent = opponent;
    }
}
