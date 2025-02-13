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

    private val _highlightedCells = MutableStateFlow<List<Pair<Int, Int>>>(emptyList())
    val highlightedCells = _highlightedCells.asStateFlow()

    private var selected =  Pair(-1, -1)

    fun getPossibleMoves(row: Int, col: Int) {
        // TODO: actual implementation instead of placeholder
        if (selected == row to col) {
            return
        }

        val newPair = (3..4).random() to (0..7).random()
        val newSet: List<Pair<Int, Int>> = listOf(newPair)

        selected = row to col

        _highlightedCells.value = newSet
        println("clicked ($row, $col) - possible move (${newPair.first}, ${newPair.second})")
        println(newSet)
    }

    fun makeMove(row: Int, col: Int) {
        // TODO: actual implementation instead of placeholder
        _highlightedCells.value = emptyList()
        selected = -1 to -1
        println("Moved to ($row, $col)")
    }
}