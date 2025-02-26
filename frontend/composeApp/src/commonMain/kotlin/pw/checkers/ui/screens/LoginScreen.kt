package pw.checkers.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import pw.checkers.data.domain.User
import pw.checkers.data.message.Message
import pw.checkers.ui.util.messageCollectionDisposableEffect
import pw.checkers.ui.windowSize.WindowSize
import pw.checkers.ui.windowSize.rememberWindowSize
import pw.checkers.viewModel.loginScreen.LoginScreenState
import pw.checkers.viewModel.loginScreen.LoginViewModel

@Composable
fun LoginScreen(loginViewModel: LoginViewModel, onPlayClick: (Message, User) -> Unit) {
    val uiState by loginViewModel.uiState.collectAsState()
    val windowSize = rememberWindowSize()

    messageCollectionDisposableEffect(loginViewModel)

    when (uiState) {
        is LoginScreenState.Idle -> {
            LoginInputScreen(loginViewModel, windowSize)
        }

        is LoginScreenState.Queued -> {
            val state = uiState as LoginScreenState.Queued
            onPlayClick(state.message, state.userInfo)
        }
    }
}

@Composable
private fun LoginInputScreen(viewModel: LoginViewModel, windowSize: WindowSize) {

    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = viewModel.username,
            onValueChange = { viewModel.onUsernameEntered(it) },
            label = { Text(text = "Username") },
            modifier = Modifier.width((windowSize.width / 3).dp),
            singleLine = true,
            isError = viewModel.hasUserInteracted && !viewModel.checkIfValid(),
            supportingText = viewModel.errorMessage?.let { message ->
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
            keyboardActions = KeyboardActions(onDone = { viewModel.play() })
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = viewModel::play, enabled = viewModel.checkIfValid()) {
            Text(text = "Play")
        }
    }
}