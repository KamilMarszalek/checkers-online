package pw.checkers.data.message

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import pw.checkers.data.messageType.MessageType

@Serializable
data class Message(
    val type: MessageType,
    val content: JsonElement,
)
