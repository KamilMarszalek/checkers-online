package pw.checkers.app.navDto.mappers

import pw.checkers.app.navDto.dto.GameCreatedNavDto
import pw.checkers.app.navDto.dto.JoinedQueueNavDto
import pw.checkers.game.domain.GameEvent

fun GameEvent.GameCreated.toNavDto() = GameCreatedNavDto(gameId, color, opponent.toNavDto())
fun GameCreatedNavDto.toDomain() = GameEvent.GameCreated(gameId, color, opponent.toDomain())

fun GameEvent.JoinedQueue.toNavDto() = JoinedQueueNavDto(message)
fun JoinedQueueNavDto.toDomain() = GameEvent.JoinedQueue(message)