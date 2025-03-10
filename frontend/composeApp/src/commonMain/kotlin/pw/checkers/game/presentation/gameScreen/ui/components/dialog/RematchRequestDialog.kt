package pw.checkers.game.presentation.gameScreen.ui.components.dialog

import androidx.compose.runtime.Composable
import checkers.composeapp.generated.resources.Res
import checkers.composeapp.generated.resources.accept_button
import checkers.composeapp.generated.resources.decline_button
import checkers.composeapp.generated.resources.rematch_requested_title
import pw.checkers.core.presentation.UiText
import pw.checkers.game.presentation.gameScreen.GameBoardAction

@Composable
internal fun RematchRequestDialog(
    message: UiText,
    onAction: (GameBoardAction) -> Unit
) {
    ConfirmDialog(
        onDismissRequest = {},
        onDismiss = { onAction(GameBoardAction.OnRematchDeclineClick) },
        onConfirm = { onAction(GameBoardAction.OnRematchAcceptClick) },
        confirmButtonText = UiText.StringResourceId(Res.string.accept_button),
        dismissButtonText = UiText.StringResourceId(Res.string.decline_button),
        title = UiText.StringResourceId(Res.string.rematch_requested_title),
        text = message,
    )
}