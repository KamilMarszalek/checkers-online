package pw.checkers.viewModel.loginScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pw.checkers.client.RealtimeMessageClient
import pw.checkers.data.domain.User
import pw.checkers.data.message.Message
import pw.checkers.data.messageType.MessageType
import pw.checkers.data.request.JoinQueue
import pw.checkers.viewModel.BaseViewModel

class LoginViewModel(
    messageClient: RealtimeMessageClient
) : BaseViewModel<LoginScreenState>(messageClient) {

    override val _uiState = MutableStateFlow<LoginScreenState?>(LoginScreenState.Idle)
    val uiState = _uiState.asStateFlow()

    private var _username by mutableStateOf("Guest")
    val username get() = _username

    var hasUserInteracted by mutableStateOf(false)
        private set

    fun checkIfValid(): Boolean = _username.isNotEmpty() && username.length < 20

    val errorMessage: String?
        get() = when {
            !hasUserInteracted -> null
            username.isEmpty() -> "Username cannot be empty."
            username.length > 19 -> "Username must be under 20 characters."
            else -> null
        }

    fun onUsernameEntered(username: String) {
        if (!hasUserInteracted) {
            hasUserInteracted = true
        }
        _username = username
    }

    fun play() {
        val content = JoinQueue(User(username = _username))
        sendMessage(MessageType.JOIN_QUEUE, content)
    }

    override fun handleServerMessage(msg: Message) {
        when (msg.type) {
            MessageType.WAITING, MessageType.GAME_CREATED -> processWaitingMessage(msg)
            else -> {}
        }
    }

    private fun processWaitingMessage(message: Message) {
        updateState(LoginScreenState.Queued(message, User(_username)))
    }
}
