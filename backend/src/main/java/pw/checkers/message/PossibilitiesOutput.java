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
public class PossibilitiesOutput extends Message{
    private List<MoveHelper> moves;

    public PossibilitiesOutput() {
        super(MessageType.POSSIBILITIES.getValue());
        this.moves = new ArrayList<>();
    }
}
