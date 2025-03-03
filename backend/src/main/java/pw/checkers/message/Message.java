package pw.checkers.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = GameEnd.class, name = "gameEnd"),
        @JsonSubTypes.Type(value = QueueMessage.class, name = "joinQueue"),
        @JsonSubTypes.Type(value = QueueMessage.class, name = "leaveQueue"),
        @JsonSubTypes.Type(value = MoveInput.class, name = "move"),
        @JsonSubTypes.Type(value = PossibilitiesInput.class, name = "possibilities"),
        @JsonSubTypes.Type(value = GameIdMessage.class, name = "rematchRequest"),
        @JsonSubTypes.Type(value = GameIdMessage.class, name = "acceptRematch"),
        @JsonSubTypes.Type(value = GameIdMessage.class, name = "declineRematch"),
        @JsonSubTypes.Type(value = GameIdMessage.class, name = "leave"),
        @JsonSubTypes.Type(value = PromptMessage.class, name = "info"),
        @JsonSubTypes.Type(value = PromptMessage.class, name = "error"),
})
public abstract class Message {
    private String type;
}
