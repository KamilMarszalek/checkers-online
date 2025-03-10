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
import checkers.composeapp.generated.resources.Res
import checkers.composeapp.generated.resources.main_menu_button
import checkers.composeapp.generated.resources.next_game_button
import checkers.composeapp.generated.resources.rematch_button
import org.jetbrains.compose.resources.stringResource
import pw.checkers.core.presentation.UiText
import pw.checkers.game.presentation.gameScreen.GameBoardAction

@Composable
private fun BaseGameEndDialog(
    message: UiText,
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
                    text = message.asString(),
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
    message: UiText,
    onAction: (GameBoardAction) -> Unit
) {
    BaseGameEndDialog(message = message) {
        Button(
            onClick = { onAction(GameBoardAction.OnMainMenuClick) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.main_menu_button))
        }
        Button(
            onClick = { onAction(GameBoardAction.OnNextGameClick) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.next_game_button))
        }
    }
}

@Composable
internal fun GameEndDialog(
    message: UiText,
    onAction: (GameBoardAction) -> Unit
) {
    BaseGameEndDialog(message = message) {
        Button(
            onClick = { onAction(GameBoardAction.OnMainMenuClick) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.main_menu_button))
        }
        Button(
            onClick = { onAction(GameBoardAction.OnNextGameClick) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.next_game_button))
        }
        Button(
            onClick = { onAction(GameBoardAction.OnRematchRequestClick) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.rematch_button))
        }
    }
}