package pw.checkers.game.presentation.gameScreen.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.times
import pw.checkers.game.domain.model.Board
import pw.checkers.game.domain.model.Cell
import pw.checkers.game.domain.model.PieceType
import pw.checkers.game.domain.model.PlayerColor
import pw.checkers.game.presentation.gameScreen.GameBoardAction
import pw.checkers.game.presentation.gameScreen.GameState
import pw.checkers.game.presentation.gameScreen.ui.components.icon.Pawns
import pw.checkers.game.presentation.gameScreen.ui.components.icon.pawns.BlackPawn
import pw.checkers.game.presentation.gameScreen.ui.components.icon.pawns.BlackQueen
import pw.checkers.game.presentation.gameScreen.ui.components.icon.pawns.WhitePawn
import pw.checkers.game.presentation.gameScreen.ui.components.icon.pawns.WhiteQueen

@Composable
fun Board(
    board: Board,
    uiState: GameState,
    cellSize: Dp,
    modifier: Modifier = Modifier,
    onAction: (GameBoardAction) -> Unit,
) {

    Column(modifier = modifier) {
        board.forEach { row ->
            Row {
                row.forEach { cell ->
                    val isHighlighted = uiState.highlightedCells.contains(cell)
                    Cell(
                        cell,
                        cellSize,
                        isHighlighted,
                        onPieceClick = { row, col -> onAction(GameBoardAction.OnPieceClick(row, col)) },
                        onHighlightedClick = { row, col -> onAction(GameBoardAction.OnHighLightedClick(row, col)) },
                        onEmptyClick = { _, _ -> onAction(GameBoardAction.OnEmptyClick) },
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