package pw.checkers.client

import kotlinx.coroutines.flow.Flow
import pw.checkers.data.message.Message
import pw.checkers.util.Constants

interface RealtimeMessageClient {
    fun connected(): Boolean
    suspend fun connect(serverAddress: String = Constants.SERVER_ADDRESS)
    suspend fun getMessageStream(): Flow<Message>
    suspend fun sendMessage(message: Message)
    suspend fun closeConnection()
}