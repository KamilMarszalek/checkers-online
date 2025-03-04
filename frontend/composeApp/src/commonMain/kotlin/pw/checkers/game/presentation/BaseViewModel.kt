package pw.checkers.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pw.checkers.game.domain.GameAction
import pw.checkers.game.domain.GameEvent
import pw.checkers.game.domain.repository.GameRepository

abstract class BaseViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private var collectJob: Job? = null

    fun startCollecting() {
        if (collectJob?.isActive == true) return

        collectJob = viewModelScope.launch {
            gameRepository.getEvents().collect { event ->
                handleGameEvent(event)
            }
        }
    }

    fun stopCollecting() {
        collectJob?.cancel()
        collectJob = null
    }

    protected fun sendAction(action: GameAction) {
        viewModelScope.launch {
            gameRepository.sendAction(action)
        }
    }

    protected abstract fun handleGameEvent(event: GameEvent)
}