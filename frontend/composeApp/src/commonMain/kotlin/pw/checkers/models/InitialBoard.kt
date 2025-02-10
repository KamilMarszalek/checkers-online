package pw.checkers.models

typealias Board = List<List<CellState>>

val initialBoard: Board = List(8) { row ->
    List(8) { col ->
            when {
                row < 3 && (row + col) % 2 == 1 -> CellState(row, col, Piece(PieceColor.BLACK, PieceType.Pawn))
                row > 4 && (row + col) % 2 == 1 -> CellState(row, col, Piece(PieceColor.WHITE, PieceType.Pawn))
                else -> CellState(row, col)
        }
    }
}