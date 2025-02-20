package pw.checkers.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import pw.checkers.ui.Board
import pw.checkers.ui.util.messageCollectionDisposableEffect
import pw.checkers.ui.windowSize.WindowSize
import pw.checkers.ui.windowSize.rememberWindowSize
import pw.checkers.util.calcCellSize
import pw.checkers.viewModel.gameScreen.GameViewModel

@Composable
fun GameScreen(
    gameViewModel: GameViewModel,
//    onGameEnd: ()
) {
    messageCollectionDisposableEffect(gameViewModel)

    val windowSize = rememberWindowSize()
    val uiState by gameViewModel.uiState.collectAsState()

    Board(gameViewModel, windowSize)

//    when (uiState) {
//        is GameScreenState.GameEnded -> {
//            val message: String = gameViewModel.getEndGameText()
//            GameEndPopup(
//                gameViewModel,
//                message,
//                windowSize
//            )
//        }
//        else -> {}
//    }
}

@Composable
private fun Board(gameViewModel: GameViewModel, windowSize: WindowSize) {
    val cellSize = remember { calcCellSize(windowSize.width, windowSize.height) }
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

@Composable
private fun GameEndPopup(
    message: String,
    onMainMenuClick: (String) -> Unit,
    onPlayNextClick: (String) -> Unit,
    onRematchClick: () -> Unit,
) {
    Dialog(
        onDismissRequest = {}
    ) {
        Surface (
//            shape = MaterialTheme.shapes.medium,
//            tonalElevation = 8.dp
        ) {

        }
    }
}

