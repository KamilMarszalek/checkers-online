package pw.checkers.viewModel.loginScreen

import pw.checkers.data.domain.User
import pw.checkers.data.message.Message
import pw.checkers.viewModel.ScreenState


sealed interface LoginScreenState : ScreenState {
    data object Idle : LoginScreenState
    data class Queued(val message: Message, val userInfo: User) : LoginScreenState
}