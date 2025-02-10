package pw.checkers.data.request

import kotlinx.serialization.Serializable
import pw.checkers.data.Move

@Serializable
data class MakeMove(
    val gameId: String,
    val move: Move,
)
