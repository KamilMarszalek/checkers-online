package pw.checkers.game.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import pw.checkers.game.data.client.RealtimeMessageClient
import pw.checkers.game.data.mappers.messageMappers.toDomain
import pw.checkers.game.data.mappers.messageMappers.toDto
import pw.checkers.game.domain.GameAction
import pw.checkers.game.domain.GameEvent
import pw.checkers.game.domain.repository.GameRepository

// TODO handle errors when connecting
class GameRepositoryImpl(private val messageClient: RealtimeMessageClient) : GameRepository {

    init {
        CoroutineScope(Dispatchers.Default).launch {
            if (!messageClient.connected()) {
                messageClient.connect()
            }
        }
    }

    override suspend fun getEvents(): Flow<GameEvent> {
        return messageClient.getMessageStream().map { it.toDomain() }
    }

    override suspend fun sendAction(action: GameAction) {
        messageClient.sendMessage(action.toDto())
    }
}