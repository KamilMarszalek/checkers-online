package pw.checkers.game.domain.repository

import kotlinx.coroutines.flow.Flow
import pw.checkers.game.domain.GameAction
import pw.checkers.game.domain.GameEvent

interface GameRepository {
    suspend fun getEvents(): Flow<GameEvent>
    suspend fun sendAction(action: GameAction)
}