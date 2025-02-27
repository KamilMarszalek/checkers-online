package pw.checkers.viewModel.waitingScreen

import pw.checkers.data.response.GameInfo

sealed interface WaitingScreenEvent {
    data class GameCreated(val gameInfo: GameInfo) : WaitingScreenEvent
}