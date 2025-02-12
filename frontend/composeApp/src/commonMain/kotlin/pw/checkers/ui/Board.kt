package pw.checkers.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import pw.checkers.models.*
import pw.checkers.ui.icons.Pawns
import pw.checkers.ui.icons.pawns.BlackPawn
import pw.checkers.ui.icons.pawns.WhitePawn
import pw.checkers.ui.icons.pawns.WhiteQueen

@Composable
fun Board(
    board: Board,
    highlightedCells: Set<Pair<Int, Int>>,
    cellSize: Dp,
    onCellClick: (row: Int, col: Int) -> Unit,
) {
    Column {
        board.forEachIndexed { rowIndex, row ->
            Row {
                row.forEachIndexed { colIndex, cell ->
                    val isHighlighted = highlightedCells.contains(rowIndex to colIndex)
                    Cell(cell, cellSize, isHighlighted, onCellClick)
                }
            }
        }
    }
}

@Composable
private fun Cell(
    cell: CellState,
    cellSize: Dp,
    isHighlighted: Boolean,
    onClick: (row: Int, col: Int) -> Unit
) {
    Box(
        modifier = Modifier
            .size(cellSize)
            .background(if ((cell.row + cell.col) % 2 == 0) Color.LightGray else Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        cell.piece?.let { piece ->
            val icon = when (piece.color) {
                PieceColor.WHITE -> if (piece.type == PieceType.PAWN) Pawns.WhitePawn else Pawns.WhiteQueen
                PieceColor.BLACK -> if (piece.type == PieceType.PAWN) Pawns.BlackPawn else Pawns.BlackPawn
            }
            Icon(
                icon,
                contentDescription = "Checkers piece",
                modifier = Modifier.fillMaxSize().clickable { onClick(cell.row, cell.col) },
                tint = Color.Unspecified,
            )
        }
    }
}