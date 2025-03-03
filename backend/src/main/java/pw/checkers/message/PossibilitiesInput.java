package pw.checkers.message;

import lombok.Getter;
import lombok.Setter;
import pw.checkers.data.enums.MessageType;

@Getter
@Setter
public class PossibilitiesInput extends Message{
    private String gameId;
    private int row;
    private int col;

    public PossibilitiesInput() {
        super(MessageType.POSSIBILITIES.getValue());
    }
    public PossibilitiesInput(String gameId, int row, int col) {
        super(MessageType.POSSIBILITIES.getValue());
        this.gameId = gameId;
        this.row = row;
        this.col = col;
    }
}
