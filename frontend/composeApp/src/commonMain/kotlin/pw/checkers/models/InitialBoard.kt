package pw.checkers.models

import pw.checkers.data.Cell
import pw.checkers.data.Piece
import pw.checkers.data.PieceType
import pw.checkers.data.PlayerColor

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
