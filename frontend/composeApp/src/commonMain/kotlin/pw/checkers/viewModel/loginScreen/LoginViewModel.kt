package pw.checkers.viewModel.loginScreen

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pw.checkers.client.RealtimeMessageClient
import pw.checkers.data.domain.User
import pw.checkers.data.message.Message
import pw.checkers.data.messageType.MessageType
import pw.checkers.data.request.JoinQueue
import pw.checkers.viewModel.BaseViewModel

class LoginViewModel(
    messageClient: RealtimeMessageClient
) : BaseViewModel(messageClient) {

    private val _state = MutableStateFlow(LoginScreenState())
    val state = _state.asStateFlow()

    val usernameValidation: StateFlow<UserNameValidation> = state
        .map { state ->
            when {
                !state.hasUserInteracted -> UserNameValidation(false)
                state.username.isBlank() -> UserNameValidation(false, "Username cannot be empty")
                state.username.length > 20 -> UserNameValidation(false, "Username must be under 20 characters")
                else -> UserNameValidation(true)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserNameValidation(false))

    private val _events = MutableSharedFlow<LoginScreenEvent>()
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
        val content = JoinQueue(User(username = _state.value.username))
        sendMessage(MessageType.JOIN_QUEUE, content)
    }

    override fun handleServerMessage(msg: Message) {
        when (msg.type) {
            MessageType.WAITING, MessageType.GAME_CREATED -> processWaitingMessage(msg)
            else -> {}
        }
    }

    private fun processWaitingMessage(message: Message) {
        viewModelScope.launch {
            _events.emit(LoginScreenEvent.JoinQueue(message, User(_state.value.username)))
        }
    }
}
