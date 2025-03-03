package pw.checkers.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pw.checkers.data.enums.MessageType;

@Getter
@Setter
@NoArgsConstructor
public class PossibilitiesInput extends Message{
    private String gameId;
    private int row;
    private int col;

    public PossibilitiesInput(String gameId, int row, int col) {
        super(MessageType.POSSIBILITIES.getValue());
        this.gameId = gameId;
        this.row = row;
        this.col = col;
    }
}
