package pw.checkers.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import pw.checkers.client.RealtimeMessageClient
import pw.checkers.data.Content
import pw.checkers.data.message.Message
import pw.checkers.data.messageType.MessageType

abstract class BaseViewModel<T : ScreenState>(
    protected val messageClient: RealtimeMessageClient
) : ViewModel() {

    private var collectJob: Job? = null

    protected open val _uiState = MutableStateFlow<T?>(null)

    fun startCollecting() {
        if (collectJob?.isActive == true) return

        collectJob = viewModelScope.launch {
            if (!messageClient.connected()) {
                messageClient.connect()
            }

            messageClient.getMessageStream().collect { message ->
                handleServerMessage(message)
            }
        }
    }

    fun stopCollecting() {
        collectJob?.cancel()
        collectJob = null
    }

    protected inline fun <reified C : Content> sendMessage(type: MessageType, content: C) {
        val message = Message(
            type = type,
            content = Json.encodeToJsonElement<C>(content)
        )

        viewModelScope.launch {
            println("Sending: $message")
            messageClient.sendMessage(message)
        }
    }

    protected inline fun <reified C : Content> handleMessageContent(msg: Message, processFunc: (C) -> Unit) {
        val content = Json.decodeFromJsonElement<C>(msg.content)
        processFunc(content)
    }

    protected abstract fun handleServerMessage(msg: Message)

    protected fun updateState(newState: T) {
        _uiState.update { newState }
    }
}