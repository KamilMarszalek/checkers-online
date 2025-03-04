package pw.checkers.game.data.client

import kotlinx.coroutines.flow.Flow
import pw.checkers.game.data.dto.message.Incoming
import pw.checkers.game.data.dto.message.Outgoing
import pw.checkers.core.util.Constants

interface RealtimeMessageClient {
    fun connected(): Boolean
    suspend fun connect(serverAddress: String = Constants.SERVER_ADDRESS)
    suspend fun getMessageStream(): Flow<Incoming>
    suspend fun sendMessage(message: Outgoing)
    suspend fun closeConnection()
}