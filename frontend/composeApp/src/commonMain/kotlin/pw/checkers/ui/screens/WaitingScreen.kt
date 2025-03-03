package pw.checkers.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import pw.checkers.data.domain.User
import pw.checkers.data.response.GameInfo
import pw.checkers.ui.util.messageCollectionDisposableEffect
import pw.checkers.viewModel.waitingScreen.WaitingScreenState
import pw.checkers.viewModel.waitingScreen.WaitingViewModel

@Composable
fun WaitingScreen(
    waitingViewModel: WaitingViewModel,
    onGameCreated: (GameInfo, User) -> Unit,
) {
    val uiState by waitingViewModel.uiState.collectAsState()

    messageCollectionDisposableEffect(waitingViewModel)

    Box(modifier = Modifier.fillMaxSize(1f), contentAlignment = Alignment.Center) {
        when (uiState) {
            is WaitingScreenState.Waiting -> {
                val message = (uiState as WaitingScreenState.Waiting).message
                AnimatedWaitingText(message, 3)
            }
            is WaitingScreenState.GameCreated -> {
                val state = (uiState as WaitingScreenState.GameCreated)
                onGameCreated(state.gameInfo, state.user)
            }
            else -> {}
        }
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