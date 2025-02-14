package pw.checkers.data.response

import kotlinx.serialization.Serializable
import pw.checkers.data.Cell
import pw.checkers.data.Move
import pw.checkers.data.PlayerColor

@Serializable
data class MoveInfo(
    val move: Move,
    val captured: Boolean,
    val capturedPiece: Cell,
    val hasMoreTakes: Boolean,
    val currentTurn: PlayerColor,
    val previousTurn: PlayerColor
)
