package pw.checkers.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PromptMessage extends Message {
    private String message;

    public PromptMessage(String type, String message) {
        super(type);
        this.message = message;
    }
}
