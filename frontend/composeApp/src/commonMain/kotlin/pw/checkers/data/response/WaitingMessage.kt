package pw.checkers.data.response

import kotlinx.serialization.Serializable

@Serializable
data class WaitingMessage(
    val message: String,
)
