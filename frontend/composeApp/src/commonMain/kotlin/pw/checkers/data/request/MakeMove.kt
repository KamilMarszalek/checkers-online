package pw.checkers.data.request

import kotlinx.serialization.Serializable
import pw.checkers.data.Content
import pw.checkers.data.domain.Move

@Serializable
data class MakeMove(
    val gameId: String,
    val move: Move,
) : Content
