package pw.checkers.game.presentation.gameScreen.ui.components.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
internal fun ConfirmDialog(
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
