package pw.checkers.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QueueMessage extends Message {
    private User user;

    public QueueMessage(String type, User user) {
        super(type);
        this.user = user;
    }
}
