package pw.checkers.models

import pw.checkers.util.PlayerColor

typealias Board = List<List<CellState>>

fun createInitialBoard(): Board = List(8) { row ->
    List(8) { col ->
        when {
            row < 3 && (row + col) % 2 == 1 -> CellState(row, col, Piece(PlayerColor.BLACK, PieceType.PAWN))
            row > 4 && (row + col) % 2 == 1 -> CellState(row, col, Piece(PlayerColor.WHITE, PieceType.PAWN))
            else -> CellState(row, col)
        }
    }
}
