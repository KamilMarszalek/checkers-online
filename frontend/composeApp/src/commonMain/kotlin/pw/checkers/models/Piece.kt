package pw.checkers.models

import pw.checkers.util.PlayerColor

enum class PieceType() {
    PAWN, QUEEN
}

data class Piece(val color: PlayerColor, val type: PieceType)

data class CellState(
    val row: Int,
    val col: Int,
    val piece: Piece? = null,
)