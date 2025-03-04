package pw.checkers.game.data.mappers.messageMappers

import pw.checkers.game.data.dto.message.Outgoing
import pw.checkers.game.data.mappers.toDomain
import pw.checkers.game.data.mappers.toDto
import pw.checkers.game.domain.GameAction

fun GameAction.toDto(): Outgoing = when(this) {
    is GameAction.JoinQueue -> Outgoing.JoinQueue(user.toDto())
    is GameAction.LeaveQueue -> Outgoing.LeaveQueue(user.toDto())
    is GameAction.MakeMove -> Outgoing.MakeMove(gameId, move.toDto())
    is GameAction.GetPossibilities -> Outgoing.GetPossibilities(gameId, row, col)
    is GameAction.RequestRematch -> Outgoing.RequestRematch(gameId)
    is GameAction.AcceptRematch -> Outgoing.AcceptRematch(gameId)
    is GameAction.DeclineRematch -> Outgoing.DeclineRematch(gameId)
}

fun Outgoing.toDomain(): GameAction  = when(this) {
    is Outgoing.JoinQueue -> GameAction.JoinQueue(user.toDomain())
    is Outgoing.LeaveQueue -> GameAction.LeaveQueue(user.toDomain())
    is Outgoing.MakeMove -> GameAction.MakeMove(gameId, move.toDomain())
    is Outgoing.GetPossibilities -> GameAction.GetPossibilities(gameId, row, col)
    is Outgoing.RequestRematch -> GameAction.RequestRematch(gameId)
    is Outgoing.AcceptRematch -> GameAction.AcceptRematch(gameId)
    is Outgoing.DeclineRematch -> GameAction.DeclineRematch(gameId)
}