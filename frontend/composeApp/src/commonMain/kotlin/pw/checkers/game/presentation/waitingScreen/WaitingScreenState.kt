package pw.checkers.game.presentation.waitingScreen

import pw.checkers.core.presentation.UiText


data class WaitingScreenState(
    val waiting: Boolean = true,
    val message: UiText? = null,
)