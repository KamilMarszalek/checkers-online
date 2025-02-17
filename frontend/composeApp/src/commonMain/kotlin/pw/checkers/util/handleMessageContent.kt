package pw.checkers.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import pw.checkers.data.Content
import pw.checkers.data.message.Message

inline fun <reified T: Content> handleMessageContent(msg: Message, processFunc: (T) -> Unit) {
    val content = Json.decodeFromJsonElement<T>(msg.content)
    processFunc(content)
}