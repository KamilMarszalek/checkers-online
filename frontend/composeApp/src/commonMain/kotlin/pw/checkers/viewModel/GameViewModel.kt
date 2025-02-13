package pw.checkers.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import pw.checkers.data.Move
import pw.checkers.models.CellState
import pw.checkers.models.PieceType
import pw.checkers.models.createInitialBoard
import pw.checkers.util.PlayerColor
import kotlin.math.abs

class GameViewModel(val color: PlayerColor) : ViewModel() {
    private val _board = MutableStateFlow(createInitialBoard())
    val board = _board.map { board ->
        if (color == PlayerColor.BLACK) {
            board.asReversed().map { innerList -> innerList.asReversed() }
        } else {
            board
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = if (color == PlayerColor.BLACK) _board.value.asReversed() else _board.value
    )

    private val _highlightedCells = MutableStateFlow<List<Pair<Int, Int>>>(emptyList())
    val highlightedCells = _highlightedCells.asStateFlow()

    private val _currentPlayer = MutableStateFlow(PlayerColor.BLACK)
    val currentPlayer = _currentPlayer.asStateFlow()

    private var selected = Pair(-1, -1)

    @Suppress("UNUSED_PARAMETER")
    fun unselectPiece(row: Int, col: Int) {
        selected = -1 to -1
        _highlightedCells.value = emptyList()
    }

    fun getPossibleMoves(row: Int, col: Int) {
        // TODO: actual implementation instead of placeholder
        if (checkSkipClick(row, col)) {
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

        var captured: Pair<Int, Int>? = null
        if (abs(selected.first - row) > 1) {
            captured = Pair((selected.first + row) / 2, (selected.second + col) / 2)
        }
        movePiece(Move(selected.first, selected.second, row, col), captured)

        selected = -1 to -1
        println("Moved to ($row, $col)")
    }

    private fun movePiece(move: Move, captured: Pair<Int, Int>? = null) {
        val newBoard = _board.value.toMutableList()
        val newPieceRow = newBoard[move.toRow].toMutableList()
        val currentPieceRow = newBoard[move.fromRow].toMutableList()

        var currentPieceCell = currentPieceRow[move.fromCol]
        var newPieceCell = newPieceRow[move.toCol].copy(piece = currentPieceCell.piece)

        if (checkIfUpgrade(newPieceCell)) {
            newPieceCell = newPieceCell.copy(piece = newPieceCell.piece!!.copy(type = PieceType.QUEEN))
        }

        currentPieceCell = currentPieceRow[move.fromCol].copy(piece = null)

        currentPieceRow[move.fromCol] = currentPieceCell
        newPieceRow[move.toCol] = newPieceCell

        newBoard[move.toRow] = newPieceRow.toList()
        newBoard[move.fromRow] = currentPieceRow.toList()

        captured?.let {
            val capturedRow = newBoard[captured.first].toMutableList()
            capturedRow[captured.second] = capturedRow[captured.second].copy(piece = null)
            newBoard[captured.first] = capturedRow
        }

        _board.value = newBoard.toList()
    }

    private fun checkSkipClick(row: Int, col: Int): Boolean {
        return (row to col == selected || _board.value[row][col].piece!!.color != color || _currentPlayer.value != color)
    }

    private fun checkIfUpgrade(pieceCell: CellState): Boolean {
        val piece = pieceCell.piece ?: return false
        return when (piece.color to piece.type) {
            PlayerColor.BLACK to PieceType.PAWN -> pieceCell.row == 7
            PlayerColor.WHITE to PieceType.PAWN -> pieceCell.row == 0
            else -> false
        }
    }
}