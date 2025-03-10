package pw.checkers.game.presentation.waitingScreen

import androidx.lifecycle.viewModelScope
import checkers.composeapp.generated.resources.Res
import checkers.composeapp.generated.resources.opponent_found
import checkers.composeapp.generated.resources.waiting_for_opponent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pw.checkers.core.presentation.UiText
import pw.checkers.game.domain.GameEvent
import pw.checkers.game.domain.model.User
import pw.checkers.game.domain.repository.GameRepository
import pw.checkers.game.presentation.BaseViewModel
import pw.checkers.core.util.DoNothing
import pw.checkers.game.domain.GameAction

class WaitingViewModel(
    joinedQueue: GameEvent.JoinedQueue,
    val user: User,
    gameRepository: GameRepository
) : BaseViewModel(gameRepository) {

    private val _state = MutableStateFlow(WaitingScreenState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>(replay = 1)
    val events = _events.asSharedFlow()

    init {
        handleGameEvent(joinedQueue)
    }

    override fun handleGameEvent(event: GameEvent) {
        when (event) {
            is GameEvent.GameCreated -> processGameCreated(event)
            is GameEvent.JoinedQueue -> processWaitingMessage()
            else -> DoNothing
        }
    }

    private fun processWaitingMessage() {
        _state.update {
            it.copy(message = UiText.StringResourceId(Res.string.waiting_for_opponent))
        }
    }

    private fun processGameCreated(gameCreated: GameEvent.GameCreated) {
        viewModelScope.launch {
            _events.emit(gameCreated)
        }
        _state.update {
            it.copy(message = UiText.StringResourceId(Res.string.opponent_found), waiting = false)
        }
    }

    private fun leaveQueue() {
        sendAction(GameAction.LeaveQueue(user))
    }

    fun onAction(action: WaitingScreenAction) {
        when (action) {
            is WaitingScreenAction.OnBackClick -> leaveQueue()
        }
    }
}