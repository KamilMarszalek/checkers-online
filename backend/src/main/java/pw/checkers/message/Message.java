package pw.checkers.message;

import com.fasterxml.jackson.annotation.JsonProperty;
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
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = GameEndMessage.class, name = "gameEnd"),
        @JsonSubTypes.Type(value = JoinQueueMessage.class, name = "joinQueue"),
        @JsonSubTypes.Type(value = LeaveQueueMessage.class, name = "leaveQueue"),
        @JsonSubTypes.Type(value = MoveInputMessage.class, name = "move"),
        @JsonSubTypes.Type(value = PossibilitiesInputMessage.class, name = "possibilities"),
        @JsonSubTypes.Type(value = RematchRequestMessage.class, name = "rematchRequest"),
        @JsonSubTypes.Type(value = AcceptRematchMessage.class, name = "acceptRematch"),
        @JsonSubTypes.Type(value = DeclineRematchMessage.class, name = "declineRematch"),
        @JsonSubTypes.Type(value = LeaveMessage.class, name = "leave"),
        @JsonSubTypes.Type(value = PromptMessage.class, name = "info"),
        @JsonSubTypes.Type(value = PromptMessage.class, name = "error"),
        @JsonSubTypes.Type(value = ResignMessage.class, name = "resign"),
})
public abstract class Message {
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    private String type;
}
