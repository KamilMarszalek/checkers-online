package pw.checkers.viewModel.gameScreen

import pw.checkers.data.domain.Result

sealed interface GameScreenState {
    data object Game : GameScreenState
    data class GameEnded(val result: Result) : GameScreenState
}