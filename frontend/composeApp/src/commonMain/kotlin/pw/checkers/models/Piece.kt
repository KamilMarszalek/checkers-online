package pw.checkers.models

enum class PieceColor() {
    WHITE, BLACK
}

enum class PieceType() {
    PAWN, QUEEN
}

data class Piece(val color: PieceColor, val type: PieceType)

data class CellState(
    val row: Int,
    val col: Int,
    val piece: Piece? = null,
    val selected: Boolean = false
)