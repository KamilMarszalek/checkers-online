package pw.checkers.viewModel.loginScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import pw.checkers.client.RealtimeMessageClient
import pw.checkers.data.domain.User
import pw.checkers.data.message.Message
import pw.checkers.data.messageType.MessageType
import pw.checkers.data.request.JoinQueue
import pw.checkers.data.response.GameCreated
import pw.checkers.data.response.WaitingMessage

class LoginViewModel(private val messageClient: RealtimeMessageClient) : ViewModel() {

    init {
        viewModelScope.launch {
            messageClient.connect()
            messageClient.getMessageStream().collect { message ->
                handleServerMessage(message)
            }
        }
    }

    private val _uiState = MutableStateFlow<LoginScreenState>(LoginScreenState.Idle)
    val uiState = _uiState.asStateFlow()

    private var _username by mutableStateOf("")
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
        val content = Json.encodeToJsonElement(JoinQueue(User(username = _username)))
        val message = Message(
            type = MessageType.JOIN_QUEUE,
            content = content,
        )
        println(Json.encodeToString(Message.serializer(), message))
        viewModelScope.launch {
            messageClient.sendMessage(message)
        }
    }

    private fun handleServerMessage(msg: Message) {
        when (msg.type) {
            MessageType.WAITING -> {
                val content = Json.decodeFromJsonElement<WaitingMessage>(msg.content)
                _uiState.value = LoginScreenState.Queued(content.message.replace(".", ""))
            }
            MessageType.GAME_CREATED -> {
                val content = Json.decodeFromJsonElement<GameCreated>(msg.content)
                _uiState.value = LoginScreenState.GameStarted(content)
            }
            else -> {}
        }
    }
}
