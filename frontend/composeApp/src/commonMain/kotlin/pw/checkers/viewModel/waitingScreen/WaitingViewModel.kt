package pw.checkers.viewModel.waitingScreen

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pw.checkers.client.RealtimeMessageClient
import pw.checkers.data.domain.User
import pw.checkers.data.message.Message
import pw.checkers.data.messageType.MessageType
import pw.checkers.data.response.GameInfo
import pw.checkers.data.response.WaitingMessage
import pw.checkers.viewModel.BaseViewModel
import pw.checkers.viewModel.ScreenState

class WaitingViewModel(
    message: Message,
    private val user: User,
    messageClient: RealtimeMessageClient
) : BaseViewModel(messageClient) {

    init {
        println(message)
    }

    override val _uiState = MutableStateFlow<ScreenState?>(null)
    val uiState = _uiState.asStateFlow()

    override fun handleServerMessage(msg: Message) {
        when (msg.type) {
            MessageType.WAITING -> handleMessageContent<WaitingMessage>(msg, ::processWaitingMessage)
            MessageType.GAME_CREATED -> handleMessageContent<GameInfo>(msg, ::processGameCreated)
            else -> {}
        }
    }

    private fun processWaitingMessage(waitingMessage: WaitingMessage) {
        updateState(WaitingScreenState.Waiting(waitingMessage.message.replace(".", "")))
    }

    private fun processGameCreated(gameInfo: GameInfo) {
        updateState(WaitingScreenState.GameCreated(gameInfo, user))
    }
}