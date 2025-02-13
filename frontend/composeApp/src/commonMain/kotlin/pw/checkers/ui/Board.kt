package pw.checkers.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.times
import pw.checkers.models.*
import pw.checkers.ui.icons.Pawns
import pw.checkers.ui.icons.pawns.BlackPawn
import pw.checkers.ui.icons.pawns.WhitePawn
import pw.checkers.ui.icons.pawns.WhiteQueen
import pw.checkers.util.PlayerColor
import pw.checkers.viewModel.GameViewModel

@Composable
fun Board(
    viewModel: GameViewModel,
    cellSize: Dp,
) {
    val board by viewModel.board.collectAsState()
    val highlightedCells by viewModel.highlightedCells.collectAsState()

    Column {
        board.forEachIndexed { rowIndex, row ->
            Row {
                row.forEachIndexed { colIndex, cell ->
                    val isHighlighted = highlightedCells.contains(rowIndex to colIndex)
                    Cell(cell, cellSize, isHighlighted, viewModel::getPossibleMoves, viewModel::makeMove)
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
    onPieceClick: (row: Int, col: Int) -> Unit,
    onHighlightedClick: (row: Int, col: Int) -> Unit
) {
    Box(
        modifier = Modifier.size(cellSize)
            .background(if ((cell.row + cell.col) % 2 == 0) Color.LightGray else Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        if (isHighlighted) {
            Box(
                modifier = Modifier.fillMaxSize().clickable { onHighlightedClick(cell.row, cell.col) },
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(0.4 * cellSize),
                    onDraw = { drawCircle(color = Color.Red) }
                )
            }
        }

        cell.piece?.let { piece ->
            val icon = when (piece.color) {
                PlayerColor.WHITE -> if (piece.type == PieceType.PAWN) Pawns.WhitePawn else Pawns.WhiteQueen
                PlayerColor.BLACK -> if (piece.type == PieceType.PAWN) Pawns.BlackPawn else Pawns.BlackPawn
            }
            Icon(
                icon,
                contentDescription = "Checkers piece",
                modifier = Modifier.fillMaxSize().clickable { onPieceClick(cell.row, cell.col) },
                tint = Color.Unspecified,
            )
        }
    }
}