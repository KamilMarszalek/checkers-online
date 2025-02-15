package pw.checkers.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pw.checkers.data.response.GameCreated
import pw.checkers.ui.windowSize.rememberWindowSize
import pw.checkers.viewModel.LoginViewModel

@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginClick: (GameCreated) -> Unit) {
    val windowSize = rememberWindowSize()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
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
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = viewModel::play, enabled = viewModel.checkIfValid()) {
                Text(text = "Play")
            }
        }
    }
}