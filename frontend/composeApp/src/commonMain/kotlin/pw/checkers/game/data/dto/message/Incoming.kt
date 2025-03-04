package pw.checkers.game.data.dto.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.checkers.game.data.dto.*

@Serializable
sealed interface Incoming : Message {

    @Serializable
    @SerialName(MessageType.Incoming.WAITING)
    data class JoinedQueue(val message: String) : Incoming

    @Serializable
    @SerialName(MessageType.Incoming.GAME_CREATED)
    data class GameCreated(
        val gameId: String,
        val color: PlayerColorDto,
        val opponent: UserDto
    ) : Incoming

    @Serializable
    @SerialName(MessageType.Incoming.MOVE)
    data class MoveResult(
        val move: MoveDto,
        val captured: Boolean,
        val capturedPiece: CellDto? = null,
        val hasMoreTakes: Boolean,
        val currentTurn: PlayerColorDto,
        val previousTurn: PlayerColorDto
    ) : Incoming

    @Serializable
    @SerialName(MessageType.Incoming.POSSIBILITIES)
    data class PossibleMoves(val moves: List<CellDto>) : Incoming

    @Serializable
    @SerialName(MessageType.Incoming.GAME_ENDING)
    data class GameEnd(val result: ResultDto) : Incoming

    @Serializable
    @SerialName(MessageType.Incoming.REMATCH_REQUEST)
    data class RematchRequest(val gameId: String) : Incoming

    @Serializable
    @SerialName(MessageType.Incoming.REJECTION)
    data class RematchRejected(val message: String) : Incoming
}