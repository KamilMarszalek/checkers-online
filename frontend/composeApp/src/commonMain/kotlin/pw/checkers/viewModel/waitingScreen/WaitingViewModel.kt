package pw.checkers.viewModel.waitingScreen

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pw.checkers.client.RealtimeMessageClient
import pw.checkers.data.domain.User
import pw.checkers.data.message.Message
import pw.checkers.data.messageType.MessageType
import pw.checkers.data.response.GameInfo
import pw.checkers.viewModel.BaseViewModel

class WaitingViewModel(
    message: Message,
    val user: User,
    messageClient: RealtimeMessageClient
) : BaseViewModel(messageClient) {

    private val _state = MutableStateFlow(WaitingScreenState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<WaitingScreenEvent>(replay = 1)
    val events = _events.asSharedFlow()

    init {
        handleServerMessage(message)
    }

    override fun handleServerMessage(msg: Message) {
        when (msg.type) {
            MessageType.WAITING -> processWaitingMessage()
            MessageType.GAME_CREATED -> handleMessageContent(msg, ::processGameCreated)
            else -> {}
        }
    }

    private fun processWaitingMessage() {
        _state.update {
            it.copy(message = "Waiting for an opponent")
        }
    }

    private fun processGameCreated(gameInfo: GameInfo) {
        viewModelScope.launch {
            _events.emit(WaitingScreenEvent.GameCreated(gameInfo))
        }
        _state.update {
            it.copy(message = "Opponent found", waiting = false)
        }
    }
}