package pw.checkers.game.presentation.loginScreen


data class LoginScreenState(
    val username: String = "",
    val hasUserInteracted: Boolean = false,
)

data class UserNameValidation(
    val isValid: Boolean,
    val errorMessage: String? = null,
)