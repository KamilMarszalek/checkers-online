package pw.checkers.message;

import lombok.Getter;
import lombok.Setter;
import pw.checkers.data.enums.MessageType;

@Setter
@Getter
public class MoveOutputMessage extends Message{
    private Move move;
    private boolean captured;
    private MoveHelper capturedPiece;
    private boolean hasMoreTakes;
    private String currentTurn;
    private String previousTurn;

    public MoveOutputMessage() {
        super(MessageType.MOVE.getValue());
    }
}
