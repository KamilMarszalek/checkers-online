package pw.checkers.data.response

import kotlinx.serialization.Serializable
import pw.checkers.data.domain.Cell
import pw.checkers.data.domain.Move
import pw.checkers.data.domain.PlayerColor

@Serializable
data class MoveInfo(
    val move: Move,
    val captured: Boolean,
    val capturedPiece: Cell? = null,
    val hasMoreTakes: Boolean,
    val currentTurn: PlayerColor,
    val previousTurn: PlayerColor
)
