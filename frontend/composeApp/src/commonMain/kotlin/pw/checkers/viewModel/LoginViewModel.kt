package pw.checkers.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import pw.checkers.client.RealtimeMessageClient
import pw.checkers.data.domain.User
import pw.checkers.data.message.Message
import pw.checkers.util.Constants

class LoginViewModel(private val messageClient: RealtimeMessageClient) : ViewModel() {
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
        val user = User(username = _username)
        val message = Message(
            type = Constants.TYPE_JOIN_QUEUE,
            content = Json.encodeToString(user)
        )
        viewModelScope.launch {
            messageClient.sendMessage(message)
        }
    }

    private fun readMessage() {
        
    }
}
