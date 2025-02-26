package pw.checkers.data.response

import kotlinx.serialization.Serializable
import pw.checkers.data.Content

@Serializable
data class WaitingMessage(
    val message: String,
) : Content
