package pw.checkers.data.response

import kotlinx.serialization.Serializable
import pw.checkers.data.Move

@Serializable
data class MoveInfo(
    val move: Move,
    val captured: Boolean,
    val capturedRow: Int,
    val capturedCol: Int,
    val hasMoreTakes: Boolean,
    val turn: String
)
