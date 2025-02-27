package pw.checkers.viewModel.loginScreen

interface LoginScreenAction {
    data object StartGame : LoginScreenAction
    data class UsernameChanged(val username: String) : LoginScreenAction
}