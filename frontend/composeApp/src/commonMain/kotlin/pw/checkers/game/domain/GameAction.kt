package pw.checkers.game.domain

import pw.checkers.game.domain.model.Move
import pw.checkers.game.domain.model.User

sealed interface GameAction {
    data class JoinQueue(val user: User) : GameAction
    data class LeaveQueue(val user: User) : GameAction

    data class MakeMove(val gameId: String, val move: Move) : GameAction
    data class GetPossibilities(val gameId: String, val row: Int, val col: Int) : GameAction

    data class LeaveGame(val gameId: String) : GameAction
    data class RequestRematch(val gameId: String) : GameAction
    data class AcceptRematch(val gameId: String) : GameAction
    data class DeclineRematch(val gameId: String) : GameAction
}