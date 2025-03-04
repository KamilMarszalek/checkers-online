package pw.checkers.game.presentation.loginScreen

interface LoginScreenAction {
    data object StartGame : LoginScreenAction
    data class UsernameChanged(val username: String) : LoginScreenAction
}