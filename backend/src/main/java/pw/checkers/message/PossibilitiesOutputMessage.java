package pw.checkers.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pw.checkers.data.enums.MessageType;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PossibilitiesOutputMessage extends Message{
    private List<MoveHelper> moves;

    public PossibilitiesOutputMessage() {
        super(MessageType.POSSIBILITIES.getValue());
        this.moves = new ArrayList<>();
    }
}
