package pw.checkers.viewModel.loginScreen

import pw.checkers.data.message.Message


sealed interface LoginScreenState {
    data object Idle : LoginScreenState
    data class Queued(val message: Message) : LoginScreenState
}