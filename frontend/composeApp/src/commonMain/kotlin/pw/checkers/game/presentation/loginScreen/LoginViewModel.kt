package pw.checkers.game.presentation.loginScreen

import androidx.lifecycle.viewModelScope
import checkers.composeapp.generated.resources.Res
import checkers.composeapp.generated.resources.error_username_empty
import checkers.composeapp.generated.resources.error_username_too_long
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pw.checkers.core.presentation.UiText
import pw.checkers.core.util.DoNothing
import pw.checkers.game.domain.GameAction
import pw.checkers.game.domain.GameEvent
import pw.checkers.game.domain.model.User
import pw.checkers.game.domain.repository.GameRepository
import pw.checkers.game.presentation.BaseViewModel

class LoginViewModel(
    gameRepository: GameRepository
) : BaseViewModel(gameRepository) {

    private val _state = MutableStateFlow(LoginScreenState())
    val state = _state.asStateFlow()

    private lateinit var user: User
    fun getUser() = user

    val usernameValidation: StateFlow<UserNameValidation> = state
        .map { state ->
            when {
                !state.hasUserInteracted -> UserNameValidation(false)
                state.username.isBlank() -> UserNameValidation(
                    false,
                    UiText.StringResourceId(Res.string.error_username_empty)
                )

                state.username.length > 20 -> UserNameValidation(
                    false,
                    UiText.StringResourceId(Res.string.error_username_too_long)
                )

                else -> UserNameValidation(true)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserNameValidation(false))

    private val _events = MutableSharedFlow<GameEvent>()
    val events = _events.asSharedFlow()

    fun onAction(action: LoginScreenAction) {
        when (action) {
            is LoginScreenAction.StartGame -> play()
            is LoginScreenAction.UsernameChanged -> onUsernameEntered(action.username)
        }
    }

    private fun onUsernameEntered(username: String) {
        _state.update {
            it.copy(username = username, hasUserInteracted = true)
        }
    }

    private fun play() {
        user = User(username = _state.value.username)
        sendAction(GameAction.JoinQueue(user))
    }

    override fun handleGameEvent(event: GameEvent) {
        when (event) {
            is GameEvent.GameCreated, is GameEvent.JoinedQueue -> emitEvent(event)
            else -> DoNothing
        }
    }

    private fun emitEvent(event: GameEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }
}
