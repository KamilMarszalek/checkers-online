package pw.checkers.game.data.client

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import pw.checkers.game.data.dto.message.Incoming
import pw.checkers.game.data.dto.message.Outgoing
import pw.checkers.core.util.Constants


class KtorRealtimeMessageClient(private val httpClient: HttpClient) : RealtimeMessageClient {

    private var session: WebSocketSession? = null

    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "type"
        prettyPrint = true
    }

    private val _messageFlow = MutableSharedFlow<Incoming>(replay = 1)

    override suspend fun getMessageStream() = _messageFlow.asSharedFlow()

    private var collectorJob: Job? = null

    override fun connected(): Boolean = session != null

    override suspend fun connect(serverAddress: String) {
        session = httpClient.webSocketSession {
            url(Constants.SERVER_ADDRESS)
        }

        collectorJob = CoroutineScope(Dispatchers.Default).launch {
            session!!
                .incoming
                .consumeAsFlow()
                .filterIsInstance<Frame.Text>()
                .mapNotNull { println(it.readText()) ; json.decodeFromString<Incoming>(it.readText()) }
                .collect { message ->
                    _messageFlow.emit(message)
                }
        }
    }

    override suspend fun sendMessage(message: Outgoing) {
        session?.outgoing?.send(Frame.Text(Json.encodeToString(message)))
    }

    override suspend fun closeConnection() {
        collectorJob?.cancel()
        session?.close(CloseReason(CloseReason.Codes.NORMAL, "Close connection"))
        session = null
    }
}