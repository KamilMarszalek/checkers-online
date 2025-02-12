package pw.checkers.models

typealias Board = List<List<CellState>>

fun createInitialBoard(reverse: Boolean = false): Board {
    val (topColor, bottomColor) = if (reverse)
        PieceColor.WHITE to PieceColor.BLACK
    else
        PieceColor.BLACK to PieceColor.WHITE

    return List(8) { row ->
        List(8) { col ->
            CellState(
                row, col, when {
                    (row + col) % 2 == 0 -> null
                    row < 3 -> Piece(topColor, PieceType.Pawn)
                    row > 4 -> Piece(bottomColor, PieceType.Pawn)
                    else -> null
                }
            )
        }
    }
}
