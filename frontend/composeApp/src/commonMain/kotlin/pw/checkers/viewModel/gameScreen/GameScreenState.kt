package pw.checkers.viewModel.gameScreen

import pw.checkers.data.domain.Result
import pw.checkers.data.message.Message
import pw.checkers.viewModel.ScreenState

sealed interface GameScreenState : ScreenState {
    data class GameEnded(val result: Result) : GameScreenState
    data class PlayNext(val message: Message) : GameScreenState
    data object RematchPending : GameScreenState
    data object RematchRequested : GameScreenState
    data object RematchRejected : GameScreenState
}