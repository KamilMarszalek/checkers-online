package pw.checkers.game.presentation.waitingScreen.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import kotlinx.coroutines.delay
import pw.checkers.game.domain.GameEvent
import pw.checkers.game.domain.model.User
import pw.checkers.game.presentation.waitingScreen.WaitingViewModel
import pw.checkers.game.util.messageCollectionDisposableEffect
import pw.checkers.core.util.DoNothing
import pw.checkers.game.presentation.waitingScreen.WaitingScreenAction

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WaitingScreen(
    waitingViewModel: WaitingViewModel,
    toGame: (GameEvent.GameCreated, User) -> Unit,
    backToMain: () -> Unit,
) {
    val state by waitingViewModel.state.collectAsState()

    messageCollectionDisposableEffect(waitingViewModel)

    BackHandler(enabled = true) {
        waitingViewModel.onAction(WaitingScreenAction.OnBackClick)
        backToMain()
    }

    LaunchedEffect(Unit) {
        waitingViewModel.events.collect { event ->
            when (event) {
                is GameEvent.GameCreated -> toGame(event, waitingViewModel.user)
                else -> DoNothing
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(1f), contentAlignment = Alignment.Center) {
        when {
            state.waiting -> AnimatedWaitingText(state.message?.asString())
            else -> AnimatedWaitingText(state.message?.asString(), maxDotCount = 0)
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