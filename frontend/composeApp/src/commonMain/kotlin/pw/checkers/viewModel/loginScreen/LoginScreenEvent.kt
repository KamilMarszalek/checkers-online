package pw.checkers.viewModel.loginScreen

import pw.checkers.data.domain.User
import pw.checkers.data.message.Message

sealed interface LoginScreenEvent {
    data class JoinQueue(val message: Message, val userInfo: User) : LoginScreenEvent
}