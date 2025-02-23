package pw.checkers.data.request

import kotlinx.serialization.Serializable
import pw.checkers.data.Content
import pw.checkers.data.domain.GameId
import pw.checkers.data.domain.Move

@Serializable
data class MakeMove(
    val gameId: GameId,
    val move: Move,
) : Content
