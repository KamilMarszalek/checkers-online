package pw.checkers.game.presentation.gameScreen

import pw.checkers.game.domain.model.Cell
import pw.checkers.game.domain.model.PlayerColor
import pw.checkers.game.domain.model.Result
import pw.checkers.game.domain.model.ResultDetails


data class GameState(
    val currentPlayer: PlayerColor = PlayerColor.WHITE,
    val highlightedCells: List<Cell> = emptyList(),
    val gameEnded: Boolean = false,
    val result: Result? = null,
    val resultDetails: ResultDetails? = null,
    val rematchPending: Boolean = false,
    val rematchRequested: Boolean = false,
    val rematchRequestRejected: Boolean = false,
    val rematchPropositionRejected: Boolean = false,
    val ignorePopup: Boolean = false,
)