package pw.checkers.client

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import pw.checkers.data.message.Message
import pw.checkers.util.Constants

class KtorRealtimeMessageClient(private val httpClient: HttpClient) : RealtimeMessageClient {

    private var session: WebSocketSession? = null

    override suspend fun connect(serverAddress: String) {
        session = httpClient.webSocketSession {
            url(Constants.SERVER_ADDRESS)
        }
    }

    override suspend fun getMessageStream(): Flow<Message> {
        return flow {
            val messages = session!!
                .incoming
                .consumeAsFlow()
                .filterIsInstance<Frame.Text>()
                .mapNotNull { Json.decodeFromString<Message>(it.readText()) }

            emitAll(messages)
        }
    }

    override suspend fun sendMessage(message: Message) {
        session?.outgoing?.send(Frame.Text(Json.encodeToString(message)))
    }

    override suspend fun closeConnection() {
        session?.close(CloseReason(CloseReason.Codes.NORMAL, "Close connection"))
        session = null
    }
}