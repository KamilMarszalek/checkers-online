package pw.checkers.viewModel.loginScreen

import pw.checkers.data.response.GameCreated

sealed class LoginScreenState {
    data object Idle : LoginScreenState()
    data class Queued(val message: String) : LoginScreenState()
    data class GameStarted(val gameCreated: GameCreated) : LoginScreenState()
}