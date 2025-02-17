package pw.checkers.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import pw.checkers.ui.Board
import pw.checkers.ui.windowSize.rememberWindowSize
import pw.checkers.util.calcCellSize
import pw.checkers.viewModel.GameViewModel

@Composable
fun GameScreen(gameViewModel: GameViewModel) {
    val windowSize = rememberWindowSize()
    val cellSize = remember(windowSize) { calcCellSize(windowSize.width, windowSize.height) }


    Column {
        UserPanelPlaceHolder(cellSize * 8, height = cellSize)
        Board(
            gameViewModel, cellSize
        )
        UserPanelPlaceHolder(cellSize * 8, height = cellSize)
    }
}

@Composable
private fun UserPanelPlaceHolder(width: Dp, height: Dp) {
    Box(
        modifier = Modifier.width(width).height(height).background(Color.Blue)
    )
}