package pw.checkers.game.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ResultDetailsDto {
    @SerialName("noPieces") NO_PIECES,
    @SerialName("noMoves") NO_MOVES,
    @SerialName("fiftyMove") FIFTY_MOVE,
    @SerialName("threefoldRepetition") THREEFOLD_REPETITION,
    @SerialName("resignation") RESIGN
}