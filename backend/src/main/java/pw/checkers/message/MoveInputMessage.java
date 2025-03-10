package pw.checkers.message;

import lombok.*;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.sockets.MessageVisitor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MoveInputMessage extends Message implements MessageAccept{
    private String gameId;
    private Move move;

    @Override
    public void accept(MessageVisitor visitor, WebSocketSession session) throws Exception {
        visitor.visit(this, session);
    }
}
