package pw.checkers.models

typealias Board = List<List<CellState>>

fun createInitialBoard(): Board = List(8) { row ->
    List(8) { col ->
        when {
            row < 3 && (row + col) % 2 == 1 -> CellState(row, col, Piece(PieceColor.BLACK, PieceType.PAWN))
            row > 4 && (row + col) % 2 == 1 -> CellState(row, col, Piece(PieceColor.WHITE, PieceType.PAWN))
            else -> CellState(row, col)
        }
    }
}
