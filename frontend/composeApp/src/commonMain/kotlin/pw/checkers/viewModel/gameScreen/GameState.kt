package pw.checkers.viewModel.gameScreen

import pw.checkers.data.domain.Cell
import pw.checkers.data.domain.PlayerColor
import pw.checkers.data.domain.Result

data class GameState(
    val currentPlayer: PlayerColor = PlayerColor.WHITE,
    val highlightedCells: List<Cell> = emptyList(),
    val gameEnded: Boolean = false,
    val result: Result? = null,
    val rematchPending: Boolean = false,
    val rematchRequested: Boolean = false,
    val rematchRequestRejected: Boolean = false,
    val rematchPropositionRejected: Boolean = false,
)