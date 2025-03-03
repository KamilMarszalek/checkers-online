package pw.checkers.data.request

import kotlinx.serialization.Serializable
import pw.checkers.data.Content
import pw.checkers.data.domain.GameId

@Serializable
data class GetPossibilities(
    val gameId: GameId,
    val row: Int,
    val col: Int,
) : Content
