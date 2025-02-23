package pw.checkers.viewModel.waitingScreen

import pw.checkers.data.domain.User
import pw.checkers.data.response.GameInfo
import pw.checkers.viewModel.ScreenState

sealed interface WaitingScreenState : ScreenState {
    data class Waiting(val message: String) : WaitingScreenState
    data class GameCreated(val gameInfo: GameInfo, val user: User) : WaitingScreenState
}