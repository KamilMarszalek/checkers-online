package pw.checkers.game.presentation.loginScreen

import pw.checkers.core.presentation.UiText


data class LoginScreenState(
    val username: String = "",
    val hasUserInteracted: Boolean = false,
)

data class UserNameValidation(
    val isValid: Boolean,
    val error: UiText? = null,
)