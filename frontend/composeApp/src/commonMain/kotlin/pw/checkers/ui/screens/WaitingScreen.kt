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
import pw.checkers.viewModel.waitingScreen.WaitingScreenEvent
import pw.checkers.viewModel.waitingScreen.WaitingViewModel

@Composable
fun WaitingScreen(
    waitingViewModel: WaitingViewModel,
    onGameCreated: (GameInfo, User) -> Unit,
) {
    val state by waitingViewModel.state.collectAsState()

    messageCollectionDisposableEffect(waitingViewModel)

    LaunchedEffect(Unit) {
        waitingViewModel.events.collect { event ->
            when (event) {
                is WaitingScreenEvent.GameCreated -> onGameCreated(event.gameInfo, waitingViewModel.user)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(1f), contentAlignment = Alignment.Center) {
        when {
            state.waiting -> AnimatedWaitingText(state.message)
            else -> AnimatedWaitingText(state.message, maxDotCount = 0)
        }
    }
}

@Composable
private fun AnimatedWaitingText(message: String? = null, maxDotCount: Int = 3) {
    var dotCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            dotCount = (dotCount + 1) % (maxDotCount + 1)
        }
    }
    message?.let { Text(text = it + ".".repeat(dotCount)) }
}