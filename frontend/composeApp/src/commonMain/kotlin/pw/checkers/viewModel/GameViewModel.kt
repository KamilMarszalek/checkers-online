package pw.checkers.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import pw.checkers.models.createInitialBoard
import pw.checkers.util.PlayerColor

class GameViewModel(val color: PlayerColor) : ViewModel() {
    private val _board = MutableStateFlow(createInitialBoard())
    val board = _board
        .map { board ->
            if (color == PlayerColor.BLACK) {
                board.asReversed().map { innerList -> innerList.asReversed() }
            } else {
                board
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = if (color == PlayerColor.BLACK) _board.value.asReversed() else _board.value
        )

//    private val

    fun getPossibleMoves(row: Int, col: Int) {}
}