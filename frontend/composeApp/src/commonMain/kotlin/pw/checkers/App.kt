package pw.checkers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import pw.checkers.data.PlayerColor
import pw.checkers.ui.Board
import pw.checkers.ui.theme.AppTheme
import pw.checkers.ui.windowSize.rememberWindowSize
import pw.checkers.util.*
import pw.checkers.viewModel.GameViewModel

@Composable
fun App() {
    val windowSize = rememberWindowSize()
    val cellSize = remember(windowSize) { calcCellSize(windowSize.width, windowSize.height) }
    val viewModel = GameViewModel(color = PlayerColor.BLACK)
    AppTheme {
        Scaffold {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column {
                    UserPanelPlaceHolder(cellSize * 8, height = cellSize)
                    Board(
                        viewModel, cellSize
                    )
                    UserPanelPlaceHolder(cellSize * 8, height = cellSize)
                }
            }
        }
    }
}

@Composable
fun UserPanelPlaceHolder(width: Dp, height: Dp) {
    Box(
        modifier = Modifier.width(width).height(height).background(Color.Blue)
    )
}