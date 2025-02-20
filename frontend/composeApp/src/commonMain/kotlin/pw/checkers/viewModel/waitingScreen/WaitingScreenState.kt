package pw.checkers.viewModel.waitingScreen

import pw.checkers.data.response.GameInfo

sealed interface WaitingScreenState {
    data class Waiting(val message: String) : WaitingScreenState
    data class GameCreated(val gameInfo: GameInfo) : WaitingScreenState
    data object RematchRejected : WaitingScreenState
}