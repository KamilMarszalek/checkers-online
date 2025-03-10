package pw.checkers.game.presentation.waitingScreen

sealed interface WaitingScreenAction {
    data object OnBackClick : WaitingScreenAction
}