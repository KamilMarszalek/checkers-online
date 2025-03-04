package pw.checkers.game.domain

import pw.checkers.game.domain.model.*

sealed interface GameEvent {
    data class JoinedQueue(val message: String) : GameEvent
    data class GameCreated(val gameId: String, val color: PlayerColor, val opponent: User) : GameEvent

    data class MoveResult(
        val move: Move,
        val captured: Boolean,
        val capturedPiece: Cell? = null,
        val hasMoreTakes: Boolean,
        val currentTurn: PlayerColor,
        val previousTurn: PlayerColor
    ) : GameEvent

    data class PossibleMoves(val moves: List<Cell>) : GameEvent

    data class GameEnd(val result: Result) : GameEvent

    data class RematchRequest(val gameId: String) : GameEvent
    data class RematchRejected(val message: String) : GameEvent
}