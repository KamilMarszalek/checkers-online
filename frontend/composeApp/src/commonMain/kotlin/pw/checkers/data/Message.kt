package pw.checkers.data

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val type: String,
    val content: String,
)
