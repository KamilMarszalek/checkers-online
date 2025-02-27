package pw.checkers.viewModel.gameScreen

sealed interface GameAction {
    data class MakeMove(val row: Int, val col: Int) : GameAction
    data class GetPossibleMoves(val row: Int, val col: Int) : GameAction
    data class UnselectPiece(val row: Int, val col: Int) : GameAction

    data object RequestRematch : GameAction
    data object AcceptRematch : GameAction
    data object DeclineRematch : GameAction

    data object PlayNext : GameAction
    data object MainMenu : GameAction
}