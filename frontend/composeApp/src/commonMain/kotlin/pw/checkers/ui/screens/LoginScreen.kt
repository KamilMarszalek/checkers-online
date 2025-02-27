package pw.checkers.ui.screens

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
import pw.checkers.data.domain.User
import pw.checkers.data.message.Message
import pw.checkers.ui.util.messageCollectionDisposableEffect
import pw.checkers.ui.windowSize.WindowSize
import pw.checkers.ui.windowSize.rememberWindowSize
import pw.checkers.viewModel.loginScreen.*

@Composable
fun LoginScreen(loginViewModel: LoginViewModel, onPlayClick: (Message, User) -> Unit) {

    val state by loginViewModel.state.collectAsState()
    val userNameValidation by loginViewModel.usernameValidation.collectAsState()
    val windowSize = rememberWindowSize()

    messageCollectionDisposableEffect(loginViewModel)

    LaunchedEffect(Unit) {
        loginViewModel.events.collect { event ->
            when (event) {
                is LoginScreenEvent.JoinQueue -> {
                    onPlayClick(event.message, event.userInfo)
                }
            }
        }
    }

    LoginInputScreen(
        state = state,
        userNameValidation = userNameValidation,
        windowSize = windowSize,
        loginViewModel::onAction
    )
}

@Composable
private fun LoginInputScreen(
    state: LoginScreenState,
    userNameValidation: UserNameValidation,
    windowSize: WindowSize,
    onAction: (LoginScreenAction) -> Unit,
) {

    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = state.username,
            onValueChange = { onAction(LoginScreenAction.UsernameChanged(it)) },
            label = { Text(text = "Username") },
            modifier = Modifier.width((windowSize.width / 3).dp),
            singleLine = true,
            isError = !userNameValidation.errorMessage.isNullOrBlank(),
            supportingText = userNameValidation.errorMessage?.let { message ->
                {
                    Text(
                        text = message,
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
            Text(text = "Play")
        }
    }
}