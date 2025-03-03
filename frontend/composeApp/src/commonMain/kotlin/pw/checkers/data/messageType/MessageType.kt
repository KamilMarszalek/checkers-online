package pw.checkers.data.messageType

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MessageType {

    @SerialName("Game created")
    GAME_CREATED,

    @SerialName("move")
    MOVE,

    @SerialName("possibilities")
    POSSIBILITIES,

    @SerialName("waiting")
    WAITING,

    @SerialName("gameEnd")
    GAME_ENDING,

    @SerialName("rejection")
    REJECTION,

    @SerialName("joinQueue")
    JOIN_QUEUE,

    @SerialName("leaveQueue")
    LEAVE_QUEUE,

    @SerialName("rematch request")
    REMATCH_REQUEST,

    @SerialName("accept rematch")
    ACCEPT_REMATCH,

    @SerialName("decline rematch")
    DECLINE_REMATCH,

    @SerialName("leave")
    LEAVE,
}