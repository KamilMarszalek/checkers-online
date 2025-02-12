package pw.checkers.models

enum class PieceColor(name: String) {
    WHITE("white"), BLACK("black")
}

enum class PieceType(name: String) {
    Pawn("Pawn"), Queen("Queen")
}

data class Piece(val color: PieceColor, val type: PieceType)

data class CellState(
    val row: Int,
    val col: Int,
    val piece: Piece? = null,
    val selected: Boolean = false
)