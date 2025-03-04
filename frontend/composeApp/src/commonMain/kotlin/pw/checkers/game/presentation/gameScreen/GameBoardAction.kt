package pw.checkers.game.presentation.gameScreen

sealed interface GameBoardAction {
    data class OnPieceClick(val row: Int, val col: Int) : GameBoardAction
    data class OnHighLightedClick(val row: Int, val col: Int) : GameBoardAction
    data object OnEmptyClick : GameBoardAction

    data object OnMainMenuClick : GameBoardAction
    data object OnNextGameClick : GameBoardAction
    data object OnRematchRequestClick : GameBoardAction
    data object OnRematchAcceptClick : GameBoardAction
    data object OnRematchDeclineClick : GameBoardAction
}