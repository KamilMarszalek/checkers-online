package pw.checkers.viewModel.loginScreen


data class LoginScreenState(
    val username: String = "",
    val hasUserInteracted: Boolean = false,
)

data class UserNameValidation(
    val isValid: Boolean,
    val errorMessage: String? = null,
)