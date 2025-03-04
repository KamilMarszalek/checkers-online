package pw.checkers.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GameIdMessage extends Message {
    private String gameId;

    public GameIdMessage(String gameId) {
        this.gameId = gameId;
    }

    public GameIdMessage(String type, String gameId) {
        super(type);
        this.gameId = gameId;
    }
}
