package pw.checkers.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pw.checkers.models.*

@Composable
fun Board(
    board: Board,
    highlightedCells: Set<Pair<Int, Int>>,
    onCellClick: (row: Int, col: Int) -> Unit
) {
    Column {
        board.forEachIndexed { rowIndex, row ->
            Row {
                row.forEachIndexed { colIndex, cell ->
                    val isHighlighted = highlightedCells.contains(rowIndex to colIndex)
                    Cell(cell, isHighlighted, onCellClick)
                }
            }
        }
    }
}

@Composable
private fun Cell(
    cell: CellState,
    isHighlighted: Boolean,
    onClick: (row: Int, col: Int) -> Unit
) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(if ((cell.row + cell.col) % 2 == 0) Color.LightGray else Color.DarkGray)
            .clickable { onClick(cell.row, cell.col) },
        contentAlignment = Alignment.Center
    ) {
        cell.piece?.let { piece ->
//            val asset = when (piece.color) {
//                PieceColor.WHITE -> if (piece.type == PieceType.Pawn) Res.drawable.whitePawn else Res.drawable.whiteQueen
//                PieceColor.BLACK -> if (piece.type == PieceType.Pawn) Res.drawable.blackPawn else Res.drawable.blackQueen
//            }
//            Image(
//                painter = painterResource(asset),
//                contentDescription = "Checkers piece",
//                modifier = Modifier.fillMaxSize()
//            )
        }
    }
}