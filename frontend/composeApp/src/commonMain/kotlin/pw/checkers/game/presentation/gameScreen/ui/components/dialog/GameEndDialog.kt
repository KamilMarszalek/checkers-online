package pw.checkers.game.presentation.gameScreen.ui.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import pw.checkers.game.presentation.gameScreen.GameBoardAction

@Composable
private fun BaseGameEndDialog(
    message: String,
    buttonContent: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = {}) {
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
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                    textAlign = TextAlign.Center
                )
                buttonContent()
            }
        }
    }
}

@Composable
internal fun GameEndDialogNoRematch(
    message: String,
    onAction: (GameBoardAction) -> Unit
) {
    BaseGameEndDialog(message = message) {
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

@Composable
internal fun GameEndDialog(
    message: String,
    onAction: (GameBoardAction) -> Unit
) {
    BaseGameEndDialog(message = message) {
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