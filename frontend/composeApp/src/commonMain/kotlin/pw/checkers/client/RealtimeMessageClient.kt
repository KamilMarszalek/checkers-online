package pw.checkers.client

import kotlinx.coroutines.flow.Flow
import pw.checkers.data.message.Message

interface RealtimeMessageClient {
    suspend fun getMessageStream(): Flow<Message>
    suspend fun sendMessage(message: Message)
    suspend fun closeConnection()
}