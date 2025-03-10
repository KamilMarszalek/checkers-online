package pw.checkers.game.presentation.loginScreen.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import checkers.composeapp.generated.resources.Res
import checkers.composeapp.generated.resources.play_button
import checkers.composeapp.generated.resources.username_hint
import org.jetbrains.compose.resources.stringResource
import pw.checkers.game.domain.GameEvent
import pw.checkers.game.domain.model.User
import pw.checkers.game.presentation.loginScreen.LoginScreenAction
import pw.checkers.game.presentation.loginScreen.LoginScreenState
import pw.checkers.game.presentation.loginScreen.LoginViewModel
import pw.checkers.game.presentation.loginScreen.UserNameValidation
import pw.checkers.game.util.messageCollectionDisposableEffect
import pw.checkers.core.util.DoNothing

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    toWaiting: (GameEvent.JoinedQueue, User) -> Unit,
    toGame: (GameEvent.GameCreated, User) -> Unit
) {

    val state by loginViewModel.state.collectAsState()
    val userNameValidation by loginViewModel.usernameValidation.collectAsState()

    messageCollectionDisposableEffect(loginViewModel)

    LaunchedEffect(Unit) {
        loginViewModel.events.collect { event ->
            when (event) {
                is GameEvent.GameCreated -> toGame(event, loginViewModel.getUser())
                is GameEvent.JoinedQueue -> toWaiting(event, loginViewModel.getUser())
                else -> DoNothing
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(1f), contentAlignment = Alignment.Center) {
        LoginInputScreen(
            state = state,
            userNameValidation = userNameValidation,
            loginViewModel::onAction
        )
    }
}

@Composable
private fun LoginInputScreen(
    state: LoginScreenState,
    userNameValidation: UserNameValidation,
    onAction: (LoginScreenAction) -> Unit,
) {

    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = state.username,
            onValueChange = { onAction(LoginScreenAction.UsernameChanged(it)) },
            label = { Text(stringResource(Res.string.username_hint)) },
            singleLine = true,
            isError = userNameValidation.error != null,
            supportingText = userNameValidation.error?.let { error ->
                {
                    Text(
                        text = error.asString(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAction(LoginScreenAction.StartGame) })
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onAction(LoginScreenAction.StartGame) }, enabled = userNameValidation.isValid) {
            Text(stringResource(Res.string.play_button))
        }
    }
}