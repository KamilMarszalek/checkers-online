package pw.checkers.game.presentation.gameScreen.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import pw.checkers.game.util.messageCollectionDisposableEffect


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GameScreen(
    gameViewModel: GameViewModel,
    backToMain: () -> Unit,
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

    var showLeaveGameDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        showLeaveGameDialog = true
    }

    if (showLeaveGameDialog) {
        ConfirmDialog(
            onDismissRequest = { showLeaveGameDialog = false },
            onDismiss = { showLeaveGameDialog = false },
            onConfirm = {
                gameViewModel.ignoreGameEndPopup()
                gameViewModel.onAction(GameBoardAction.OnResignClick)
                gameViewModel.onAction(GameBoardAction.OnMainMenuClick)
                backToMain()
            },
            title = "Leave Game",
            text = "Are you sure you want to leave the game?",
            icon = Icons.Default.Warning,
        )
    }

    val board by gameViewModel.board.collectAsStateWithLifecycle()
    val state by gameViewModel.state.collectAsStateWithLifecycle()

    Game(
        board = board,
        state = state,
        onAction = gameViewModel::onAction,
        gameViewModel.user,
        gameViewModel.opponent,
        gameViewModel.color
    )

    if (state.gameEnded && !state.ignorePopup) EndGamePopupFromState(state, gameViewModel, backToMain)
}


@Composable
private fun Game(
    board: Board,
    state: GameState,
    onAction: (GameBoardAction) -> Unit,
    user: User,
    opponent: User,
    assignedColor: PlayerColor
) {
    var showResignPopup by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val cellSize = if (maxHeight <= maxWidth) {
            maxHeight / (board.size + 2)
        } else {
            maxWidth / board.size
        }

        Column(
            modifier = Modifier.width(cellSize * board.size),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(cellSize)) {
                UserPanel(
                    user = opponent,
                    isCurrentTurn = assignedColor != state.currentPlayer,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Box(
                modifier = Modifier.size(cellSize * board.size),
            ) {
                Board(
                    board = board,
                    uiState = state,
                    cellSize = cellSize,
                    modifier = Modifier.fillMaxSize(),
                    onAction = { action -> onAction(action) },
                )
            }

            Box(modifier = Modifier.fillMaxWidth().height(cellSize)) {
                UserPanel(
                    user = user,
                    isCurrentTurn = assignedColor == state.currentPlayer,
                    modifier = Modifier.fillMaxSize(),
                )

                Button(
                    onClick = { showResignPopup = true },
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.padding(8.dp).align(Alignment.CenterEnd),
                ) {
                    Text("Resign")
                }
            }
        }
    }

    if (showResignPopup) {
        ConfirmDialog(
            onDismissRequest = { showResignPopup = false },
            onDismiss = { showResignPopup = false },
            onConfirm = {
                showResignPopup = false
                onAction(GameBoardAction.OnResignClick)
            },
            title = "Resign game",
            text = "Are you sure you want to resign?",
        )
    }
}

@Composable
private fun ConfirmDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmButtonText: String = "Yes",
    dismissButtonText: String = "No",
    title: String? = null,
    text: String? = null,
    icon: ImageVector? = null
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(dismissButtonText)
            }
        },
        title = {
            if (title != null) {
                Text(title)
            }
        },
        text = {
            if (text != null) {
                Text(text)
            }
        },
        icon = {
            if (icon != null) {
                Icon(icon, contentDescription = null)
            }
        }
    )
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
                Button(
                    onClick = { onAction(GameBoardAction.OnMainMenuClick) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Main menu")
                }
                Button(
                    onClick = { onAction(GameBoardAction.OnNextGameClick) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Next game")
                }
                Button(
                    onClick = { onAction(GameBoardAction.OnRematchRequestClick) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Rematch")
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
                Button(
                    onClick = { onAction(GameBoardAction.OnMainMenuClick) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Main menu")
                }
                Button(
                    onClick = { onAction(GameBoardAction.OnNextGameClick) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Next game")
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
    ConfirmDialog(
        onDismissRequest = {},
        onDismiss = { onAction(GameBoardAction.OnRematchDeclineClick) },
        onConfirm = { onAction(GameBoardAction.OnRematchAcceptClick) },
        confirmButtonText = "Accept",
        dismissButtonText = "Decline",
        title = "Rematch request",
        text = message,
    )
}