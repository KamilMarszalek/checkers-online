package pw.checkers.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.data.enums.MessageType;
import pw.checkers.sockets.MessageVisitor;

@Getter
@Setter
@NoArgsConstructor
public class PossibilitiesInput extends Message implements MessageAccept{
    private String gameId;
    private int row;
    private int col;

    public PossibilitiesInput(String gameId, int row, int col) {
        super(MessageType.POSSIBILITIES.getValue());
        this.gameId = gameId;
        this.row = row;
        this.col = col;
    }

    @Override
    public void accept(MessageVisitor visitor, WebSocketSession session) throws Exception {
        visitor.visit(this, session);
    }
}
