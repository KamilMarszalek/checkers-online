package pw.checkers.viewModel.gameScreen

import pw.checkers.data.message.Message

sealed interface GameEvent {
    data class NextGame(val message: Message) : GameEvent
}