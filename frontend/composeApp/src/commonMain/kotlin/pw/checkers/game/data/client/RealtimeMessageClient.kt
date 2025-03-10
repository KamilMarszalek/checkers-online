package pw.checkers.game.data.client

import kotlinx.coroutines.flow.Flow
import pw.checkers.game.data.dto.message.Incoming
import pw.checkers.game.data.dto.message.Outgoing

interface RealtimeMessageClient {
    val serverAddress: String
    fun connected(): Boolean
    suspend fun connect()
    suspend fun getMessageStream(): Flow<Incoming>
    suspend fun sendMessage(message: Outgoing)
    suspend fun closeConnection()
}