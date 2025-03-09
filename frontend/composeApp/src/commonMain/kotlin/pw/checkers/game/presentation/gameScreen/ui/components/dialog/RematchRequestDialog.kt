package pw.checkers.game.presentation.gameScreen.ui.components.dialog

import androidx.compose.runtime.Composable
import pw.checkers.game.presentation.gameScreen.GameBoardAction

@Composable
internal fun RematchRequestDialog(
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