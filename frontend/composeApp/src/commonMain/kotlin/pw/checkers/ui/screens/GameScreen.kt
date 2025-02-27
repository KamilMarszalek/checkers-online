package pw.checkers.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pw.checkers.data.domain.User
import pw.checkers.data.message.Message
import pw.checkers.models.Board
import pw.checkers.ui.Board
import pw.checkers.ui.util.messageCollectionDisposableEffect
import pw.checkers.ui.windowSize.WindowSize
import pw.checkers.ui.windowSize.rememberWindowSize
import pw.checkers.util.calcCellSize
import pw.checkers.viewModel.gameScreen.GameEvent
import pw.checkers.viewModel.gameScreen.GameAction
import pw.checkers.viewModel.gameScreen.GameState
import pw.checkers.viewModel.gameScreen.GameViewModel

// TODO: make popups responsive, stack buttons in column when screen to narrow

@Composable
fun GameScreen(
    gameViewModel: GameViewModel,
    onMainMenuClick: () -> Unit,
    nextGame: (Message, User) -> Unit,
) {
    messageCollectionDisposableEffect(gameViewModel)

    LaunchedEffect(Unit) {
        gameViewModel.events.collect { event ->
            when (event) {
                is GameEvent.NextGame -> nextGame(event.message, gameViewModel.user)
            }
        }
    }

    val windowSize = rememberWindowSize()
    val board by gameViewModel.board.collectAsStateWithLifecycle()
    val state by gameViewModel.state.collectAsStateWithLifecycle()

    Game(
        board = board,
        state = state,
        onAction = gameViewModel::onAction,
        windowSize = windowSize,
    )

    if (state.gameEnded) EndGamePopupFromState(state, gameViewModel, onMainMenuClick)
}


@Composable
private fun Game(board: Board, state: GameState, onAction: (GameAction) -> Unit, windowSize: WindowSize) {
    val cellSize = remember { calcCellSize(windowSize.width, windowSize.height) }

    Column {
        UserPanelPlaceHolder(cellSize * 8, height = cellSize)
        Board(
            board = board,
            uiState = state,
            cellSize = cellSize,
            onAction = { action -> onAction(action) },
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
private fun EndGamePopupFromState(
    state: GameState,
    gameViewModel: GameViewModel,
    onMainMenuClick: () -> Unit
) {
    val handleAction: (GameAction) -> Unit = { action ->
        when (action) {
            GameAction.MainMenu -> onMainMenuClick()
            else -> Unit
        }
        gameViewModel.onAction(action)
    }

    when {
        state.rematchPending -> RematchPendingPopup()
        state.rematchRequested -> {
            RematchRequestPopup(
                message = gameViewModel.getRematchRequestMessage(),
                onAction = handleAction
            )
        }

        state.rematchRequestRejected -> {
            GameEndPopupNoRematch(
                message = "Game over",
                onAction = handleAction
            )
        }

        state.rematchPropositionRejected -> {
            GameEndPopupNoRematch(
                message = "Your rematch has been rejected",
                onAction = handleAction
            )
        }

        state.rematchRequested -> {
            RematchRequestPopup(
                message = gameViewModel.getRematchRequestMessage(),
                onAction = handleAction
            )
        }

        else -> {
            GameEndPopup(
                message = gameViewModel.getEndGameText(),
                onAction = handleAction
            )
        }
    }
}


@Composable
private fun GameEndPopup(
    message: String,
    onAction: (GameAction) -> Unit
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
                    Button(onClick = { onAction(GameAction.MainMenu) }) {
                        Text("Main menu")
                    }
                    Button(onClick = { onAction(GameAction.PlayNext) }) {
                        Text("Next game")
                    }
                    Button(onClick = { onAction(GameAction.RequestRematch) }) {
                        Text("Rematch")
                    }
                }
            }
        }
    }
}

@Composable
private fun RematchPendingPopup() {
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
                    text = "Waiting for response",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun GameEndPopupNoRematch(
    message: String,
    onAction: (GameAction) -> Unit
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
                    Button(onClick = { onAction(GameAction.MainMenu) }) {
                        Text("Main menu")
                    }
                    Button(onClick = { onAction(GameAction.PlayNext) }) {
                        Text("Next game")
                    }
                }
            }
        }
    }
}

@Composable
private fun RematchRequestPopup(
    message: String,
    onAction: (GameAction) -> Unit
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
                    Button(onClick = { onAction(GameAction.AcceptRematch) }) {
                        Text("Accept")
                    }
                    Button(onClick = { onAction(GameAction.DeclineRematch) }) {
                        Text("Decline")
                    }
                }
            }
        }
    }
}