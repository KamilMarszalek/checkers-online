package pw.checkers.game.domain.model

typealias Board = List<List<Cell>>

fun createInitialBoard(): Board = List(8) { row ->
    List(8) { col ->
        when {
            row < 3 && (row + col) % 2 == 1 -> Cell(row, col, Piece(PlayerColor.BLACK, PieceType.PAWN))
            row > 4 && (row + col) % 2 == 1 -> Cell(row, col, Piece(PlayerColor.WHITE, PieceType.PAWN))
            else -> Cell(row, col)
        }
    }
}
