package pw.checkers.game.presentation.gameScreen.ui.components.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import checkers.composeapp.generated.resources.Res
import checkers.composeapp.generated.resources.no_button
import checkers.composeapp.generated.resources.yes_button
import pw.checkers.core.presentation.UiText

@Composable
internal fun ConfirmDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmButtonText: UiText = UiText.StringResourceId(Res.string.yes_button),
    dismissButtonText: UiText = UiText.StringResourceId(Res.string.no_button),
    title: UiText? = null,
    text: UiText? = null,
    icon: ImageVector? = null
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
            ) {
                Text(confirmButtonText.asString())
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(dismissButtonText.asString())
            }
        },
        title = {
            if (title != null) {
                Text(title.asString())
            }
        },
        text = {
            if (text != null) {
                Text(text.asString())
            }
        },
        icon = {
            if (icon != null) {
                Icon(icon, contentDescription = null)
            }
        }
    )
}
