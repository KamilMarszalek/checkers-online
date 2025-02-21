package pw.checkers.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import pw.checkers.data.domain.User
import pw.checkers.data.message.Message
import pw.checkers.ui.Board
import pw.checkers.ui.util.messageCollectionDisposableEffect
import pw.checkers.ui.windowSize.WindowSize
import pw.checkers.ui.windowSize.rememberWindowSize
import pw.checkers.util.calcCellSize
import pw.checkers.viewModel.gameScreen.GameScreenState
import pw.checkers.viewModel.gameScreen.GameViewModel

@Composable
fun GameScreen(
    gameViewModel: GameViewModel,
    onMainMenuClick: () -> Unit,
    nextGame: (Message, User) -> Unit,
    rematch: () -> Unit
) {
    messageCollectionDisposableEffect(gameViewModel)

    val windowSize = rememberWindowSize()
    val uiState by gameViewModel.uiState.collectAsState()

    Board(gameViewModel, windowSize)

    when (uiState) {
        is GameScreenState.GameEnded -> {
            val message: String = gameViewModel.getEndGameText()
            GameEndPopup(
                message,
                onMainMenuClick,
                gameViewModel::playNextGame,
                rematch = {}
            )
        }
        is GameScreenState.PlayNext -> {
            val state = uiState as GameScreenState.PlayNext
            nextGame(state.message, gameViewModel.user)
        }
        else -> {}
    }
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
    mainMenu: () -> Unit,
    nextGame: () -> Unit,
    rematch: () -> Unit,
) {
    Dialog(
        onDismissRequest = {}
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 8.dp,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Button(onClick = mainMenu) {
                        Text("Main menu")
                    }
                    Button(onClick = nextGame) {
                        Text("Next game")
                    }
                    Button(onClick = rematch) {
                        Text("Rematch")
                    }
                }
            }
        }
    }
}

