package pw.checkers.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import pw.checkers.data.response.GameCreated
import pw.checkers.ui.windowSize.rememberWindowSize
import pw.checkers.viewModel.loginScreen.LoginScreenState
import pw.checkers.viewModel.loginScreen.LoginViewModel
import kotlin.math.min

@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginClick: (GameCreated) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is LoginScreenState.Idle -> {
            LoginInputScreen(viewModel)
        }

        is LoginScreenState.Queued -> {

            LoadingScreen((uiState as LoginScreenState.Queued).message)
        }

        is LoginScreenState.GameStarted -> {
            onLoginClick((uiState as LoginScreenState.GameStarted).gameCreated)
        }
    }
}

@Composable
private fun LoginInputScreen(viewModel: LoginViewModel) {
    val windowSize = rememberWindowSize()

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
            })
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = viewModel::play, enabled = viewModel.checkIfValid()) {
            Text(text = "Play")
        }
    }
}

@Composable
private fun LoadingScreen(message: String) {
    val windowSize = rememberWindowSize()
    val boxSize = remember(windowSize) { (0.2 * min(windowSize.width, windowSize.height)).toInt() }
    Box(modifier = Modifier.size(boxSize.dp), contentAlignment = Alignment.Center) {
        AnimatedWaitingText(message, 3)
    }
}

@Composable
private fun AnimatedWaitingText(message: String, maxDotCount: Int) {
    var dotCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            dotCount = (dotCount + 1) % (maxDotCount + 1)
        }
    }
    Text(text = message + ".".repeat(dotCount))
}