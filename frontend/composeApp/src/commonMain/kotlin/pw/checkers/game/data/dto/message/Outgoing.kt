package pw.checkers.game.data.dto.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.checkers.game.data.dto.MoveDto
import pw.checkers.game.data.dto.UserDto

@Serializable
sealed interface Outgoing {

    @Serializable
    @SerialName(MessageType.Outgoing.JOIN_QUEUE)
    data class JoinQueue(val user: UserDto) : Outgoing

    @Serializable
    @SerialName(MessageType.Outgoing.LEAVE_QUEUE)
    data class LeaveQueue(val user: UserDto) : Outgoing

    @Serializable
    @SerialName(MessageType.Outgoing.MOVE)
    data class MakeMove(val gameId: String, val move: MoveDto) : Outgoing

    @Serializable
    @SerialName(MessageType.Outgoing.POSSIBILITIES)
    data class GetPossibilities(val gameId: String, val row: Int, val col: Int) : Outgoing

    @Serializable
    @SerialName(MessageType.Outgoing.REMATCH_REQUEST)
    data class RequestRematch(val gameId: String) : Outgoing

    @Serializable
    @SerialName(MessageType.Outgoing.ACCEPT_REMATCH)
    data class AcceptRematch(val gameId: String) : Outgoing

    @Serializable
    @SerialName(MessageType.Outgoing.DECLINE_REMATCH)
    data class DeclineRematch(val gameId: String) : Outgoing

}