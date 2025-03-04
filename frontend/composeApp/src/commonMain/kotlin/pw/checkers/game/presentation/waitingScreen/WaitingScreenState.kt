package pw.checkers.game.presentation.waitingScreen


data class WaitingScreenState(
    val waiting: Boolean = true,
    val message: String? = null,
)