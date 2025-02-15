package pw.checkers.data.messageType

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MessageType {

    @SerialName("Game created")
    GAME_CREATED,

    @SerialName("move")
    MOVED,

    @SerialName("possibilities")
    POSSIBILITIES,

    @SerialName("waiting")
    WAITING,

    @SerialName("gameEnd")
    GAME_ENDING,

    @SerialName("joinQueue")
    JOIN_QUEUE,
}