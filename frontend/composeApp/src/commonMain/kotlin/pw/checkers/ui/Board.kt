package pw.checkers.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import pw.checkers.data.domain.Cell
import pw.checkers.data.domain.PieceType
import pw.checkers.ui.icons.Pawns
import pw.checkers.ui.icons.pawns.BlackPawn
import pw.checkers.ui.icons.pawns.BlackQueen
import pw.checkers.ui.icons.pawns.WhitePawn
import pw.checkers.ui.icons.pawns.WhiteQueen
import pw.checkers.data.domain.PlayerColor
import pw.checkers.viewModel.gameScreen.GameViewModel

@Composable
fun Board(
    viewModel: GameViewModel,
    cellSize: Dp,
) {
    val board by viewModel.board.collectAsState()
    val highlightedCells by viewModel.highlightedCells.collectAsState()

    Column {
        board.forEach { row ->
            Row {
                row.forEach { cell ->
                    val isHighlighted = highlightedCells.contains(cell)
                    Cell(
                        cell,
                        cellSize,
                        isHighlighted,
                        viewModel::getPossibleMoves,
                        viewModel::makeMove,
                        viewModel::unselectPiece
                    )
                }
            }
        }
    }
}

@Composable
private fun Cell(
    cell: Cell,
    cellSize: Dp,
    isHighlighted: Boolean,
    onPieceClick: (row: Int, col: Int) -> Unit,
    onHighlightedClick: (row: Int, col: Int) -> Unit,
    onEmptyClick: (row: Int, col: Int) -> Unit,
) {
    val clickFunction = when {
        isHighlighted -> onHighlightedClick
        cell.piece != null -> onPieceClick
        else -> onEmptyClick
    }
    Box(
        modifier = Modifier.size(cellSize)
            .background(if ((cell.row + cell.col) % 2 == 0) Color.LightGray else Color.DarkGray)
            .clickable(
                interactionSource = MutableInteractionSource(),
                indication = if (clickFunction == onEmptyClick) null else LocalIndication.current
            ) { clickFunction(cell.row, cell.col) },
        contentAlignment = Alignment.Center
    ) {
        if (isHighlighted) {
            Canvas(
                modifier = Modifier
                    .size(0.4 * cellSize),
                onDraw = { drawCircle(color = Color.Red) }
            )
        }

        cell.piece?.let { piece ->
            val icon = when (piece.color) {
                PlayerColor.WHITE -> if (piece.type == PieceType.PAWN) Pawns.WhitePawn else Pawns.WhiteQueen
                PlayerColor.BLACK -> if (piece.type == PieceType.PAWN) Pawns.BlackPawn else Pawns.BlackQueen
            }
            Icon(
                icon,
                contentDescription = "Checkers piece",
                modifier = Modifier.fillMaxSize(),
                tint = Color.Unspecified,
            )
        }
    }
}