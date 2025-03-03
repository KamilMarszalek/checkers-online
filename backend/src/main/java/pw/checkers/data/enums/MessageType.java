package pw.checkers.data.enums;

import lombok.Getter;

@Getter
public enum MessageType {
    JOIN_QUEUE("joinQueue"),
    LEAVE_QUEUE("leaveQueue"),
    MOVE("move"),
    POSSIBILITIES("possibilities"),
    REMATCH_REQUEST("rematchRequest"),
    ACCEPT_REMATCH("acceptRematch"),
    DECLINE_REMATCH("declineRematch"),
    LEAVE("leave"),
    REJECTION("rejection"),
    ERROR("error"),
    GAME_CREATED("gameCreated"),
    WAITING("waiting"),
    GAME_END("gameEnd"),
    UNKNOWN("unknown");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    public static MessageType fromString(String value) {
        for (MessageType mt : values()) {
            if (mt.value.equalsIgnoreCase(value)) {
                return mt;
            }
        }
        return UNKNOWN;
    }
}
