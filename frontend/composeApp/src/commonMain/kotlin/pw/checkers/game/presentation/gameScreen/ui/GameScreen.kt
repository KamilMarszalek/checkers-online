package pw.checkers.game.presentation.gameScreen.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pw.checkers.core.presentation.windowSize.WindowSize
import pw.checkers.core.presentation.windowSize.rememberWindowSize
import pw.checkers.core.util.DoNothing
import pw.checkers.game.domain.GameEvent
import pw.checkers.game.domain.model.Board
import pw.checkers.game.domain.model.PlayerColor
import pw.checkers.game.domain.model.User
import pw.checkers.game.presentation.gameScreen.GameBoardAction
import pw.checkers.game.presentation.gameScreen.GameState
import pw.checkers.game.presentation.gameScreen.GameViewModel
import pw.checkers.game.presentation.gameScreen.ui.components.Board
import pw.checkers.game.presentation.gameScreen.ui.components.UserPanel
import pw.checkers.game.util.calcCellSize
import pw.checkers.game.util.messageCollectionDisposableEffect

// TODO: make popups responsive, stack buttons in column when screen to narrow
// TODO: make better system for scaling board and user panels

@Composable
fun GameScreen(
    gameViewModel: GameViewModel,
    onMainMenuClick: () -> Unit,
    nextGame: (GameEvent, User) -> Unit,
) {
    messageCollectionDisposableEffect(gameViewModel)

    LaunchedEffect(Unit) {
        gameViewModel.events.collect { event ->
            when (event) {
                is GameEvent.GameCreated, is GameEvent.JoinedQueue -> nextGame(event, gameViewModel.user)
                else -> DoNothing
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
        gameViewModel.user,
        gameViewModel.opponent,
        gameViewModel.color
    )

    if (state.gameEnded) EndGamePopupFromState(state, gameViewModel, onMainMenuClick)
}


@Composable
private fun Game(
    board: Board,
    state: GameState,
    onAction: (GameBoardAction) -> Unit,
    windowSize: WindowSize,
    user: User,
    opponent: User,
    assignedColor: PlayerColor
) {
    val cellSize = remember { calcCellSize(windowSize.width, windowSize.height) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.weight(1f).width(cellSize * 8)) {
            UserPanel(modifier = Modifier.fillMaxSize(), opponent, assignedColor != state.currentPlayer)
        }
        Box(modifier = Modifier.size(width = cellSize * 8, height = cellSize * 8)) {
            Board(
                board = board,
                uiState = state,
                cellSize = cellSize,
                modifier = Modifier.fillMaxSize(),
                onAction = { action -> onAction(action) },
            )
        }
        Box(modifier = Modifier.weight(1f).width(cellSize * 8)) {
            UserPanel(modifier = Modifier.fillMaxSize(), user, assignedColor == state.currentPlayer)
        }
    }
}

@Composable
private fun EndGamePopupFromState(
    state: GameState,
    gameViewModel: GameViewModel,
    onMainMenuClick: () -> Unit
) {
    val handleAction: (GameBoardAction) -> Unit = { action ->
        when (action) {
            GameBoardAction.OnMainMenuClick -> onMainMenuClick()
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
                message = "Rematch request declined",
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
    onAction: (GameBoardAction) -> Unit
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
                    Button(onClick = { onAction(GameBoardAction.OnMainMenuClick) }) {
                        Text("Main menu")
                    }
                    Button(onClick = { onAction(GameBoardAction.OnNextGameClick) }) {
                        Text("Next game")
                    }
                    Button(onClick = { onAction(GameBoardAction.OnRematchRequestClick) }) {
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
    onAction: (GameBoardAction) -> Unit
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
                    Button(onClick = { onAction(GameBoardAction.OnMainMenuClick) }) {
                        Text("Main menu")
                    }
                    Button(onClick = { onAction(GameBoardAction.OnNextGameClick) }) {
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
    onAction: (GameBoardAction) -> Unit
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
                    Button(onClick = { onAction(GameBoardAction.OnRematchAcceptClick) }) {
                        Text("Accept")
                    }
                    Button(onClick = { onAction(GameBoardAction.OnRematchDeclineClick) }) {
                        Text("Decline")
                    }
                }
            }
        }
    }
}