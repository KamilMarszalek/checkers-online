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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import checkers.composeapp.generated.resources.*
import pw.checkers.core.presentation.UiText
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
import pw.checkers.game.presentation.gameScreen.ui.components.dialog.*
import pw.checkers.game.presentation.gameScreen.ui.components.dialog.ConfirmDialog
import pw.checkers.game.presentation.gameScreen.ui.components.dialog.GameEndDialog
import pw.checkers.game.presentation.gameScreen.ui.components.dialog.GameEndDialogNoRematch
import pw.checkers.game.presentation.gameScreen.ui.components.dialog.RematchRequestDialog
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
            title = UiText.StringResourceId(Res.string.leave_game_title),
            text = UiText.StringResourceId(Res.string.leave_game_text),
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

    if (state.gameEnded && !state.ignorePopup) EndGameDialogFromState(state, gameViewModel, backToMain)
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
            title = UiText.StringResourceId(Res.string.resign_game_title),
            text = UiText.StringResourceId(Res.string.resign_game_text),
        )
    }
}

@Composable
private fun EndGameDialogFromState(
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
        state.rematchPending -> RematchPendingDialog()
        state.rematchRequested -> {
            RematchRequestDialog(
                message = gameViewModel.getRematchRequestMessage(),
                onAction = handleAction
            )
        }

        state.rematchRequestRejected -> {
            GameEndDialogNoRematch(
                message = UiText.StringResourceId(Res.string.game_over),
                onAction = handleAction
            )
        }

        state.rematchPropositionRejected -> {
            GameEndDialogNoRematch(
                message = UiText.StringResourceId(Res.string.rematch_request_declined),
                onAction = handleAction
            )
        }

        else -> {
            GameEndDialog(
                message = gameViewModel.getEndGameText(),
                details = gameViewModel.getResultDetailsText(),
                onAction = handleAction
            )
        }
    }
}