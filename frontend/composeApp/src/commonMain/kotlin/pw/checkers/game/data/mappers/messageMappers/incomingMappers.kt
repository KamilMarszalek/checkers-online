package pw.checkers.game.data.mappers.messageMappers

import pw.checkers.game.data.dto.message.Incoming
import pw.checkers.game.data.mappers.toDto
import pw.checkers.game.data.mappers.toDomain
import pw.checkers.game.domain.GameEvent

fun GameEvent.toDto(): Incoming = when (this) {
    is GameEvent.JoinedQueue -> Incoming.JoinedQueue(message)
    is GameEvent.GameCreated -> Incoming.GameCreated(gameId, color.toDto(), opponent.toDto())
    is GameEvent.MoveResult -> Incoming.MoveResult(
        move.toDto(),
        captured,
        capturedPiece?.toDto(),
        hasMoreTakes,
        currentTurn.toDto(),
        previousTurn.toDto(),
    )

    is GameEvent.PossibleMoves -> Incoming.PossibleMoves(moves.map { it.toDto() })
    is GameEvent.GameEnd -> Incoming.GameEnd(result.toDto(), details.toDto())
    is GameEvent.RematchRejected -> Incoming.RematchRejected(message)
    is GameEvent.RematchRequest -> Incoming.RematchRequest(gameId)
}

fun Incoming.toDomain(): GameEvent = when(this) {
    is Incoming.JoinedQueue -> GameEvent.JoinedQueue(message)
    is Incoming.GameCreated -> GameEvent.GameCreated(gameId, color.toDomain(), opponent.toDomain())
    is Incoming.MoveResult -> GameEvent.MoveResult(
        move.toDomain(),
        captured,
        capturedPiece?.toDomain(),
        hasMoreTakes,
        currentTurn.toDomain(),
        previousTurn.toDomain(),
    )

    is Incoming.PossibleMoves -> GameEvent.PossibleMoves(moves.map { it.toDomain() })
    is Incoming.GameEnd -> GameEvent.GameEnd(result.toDomain(), details.toDomain())
    is Incoming.RematchRejected -> GameEvent.RematchRejected(message)
    is Incoming.RematchRequest -> GameEvent.RematchRequest(gameId)
}