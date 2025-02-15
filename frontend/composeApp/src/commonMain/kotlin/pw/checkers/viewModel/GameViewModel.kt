package pw.checkers.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import pw.checkers.data.domain.Move
import pw.checkers.data.domain.Cell
import pw.checkers.data.domain.PieceType
import pw.checkers.models.createInitialBoard
import pw.checkers.data.domain.PlayerColor
import kotlin.math.abs

class GameViewModel(private val color: PlayerColor, private val gameId: String = "") : ViewModel() {
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

    private val _highlightedCells = MutableStateFlow<List<Cell>>(emptyList())
    val highlightedCells = _highlightedCells.asStateFlow()

    private val _currentPlayer = MutableStateFlow(PlayerColor.WHITE)
    val currentPlayer = _currentPlayer.asStateFlow()

    private var selected = Cell(-1, -1)

    @Suppress("UNUSED_PARAMETER")
    fun unselectPiece(row: Int, col: Int) {
        selected = Cell(-1, -1)
        _highlightedCells.value = emptyList()
    }

    fun getPossibleMoves(row: Int, col: Int) {
        // TODO: actual implementation instead of placeholder
        if (checkSkipClick(row, col)) {
            return
        }

        val newCell = Cell((3..4).random(), (0..7).random())
        val newHighlighted = listOf(newCell)

        selected = Cell(row, col)

        _highlightedCells.value = newHighlighted
        println("clicked ($row, $col) - possible move (${newCell.row}, ${newCell.col})")
        println(newHighlighted)
    }

    private fun setHighlighted(cells: List<Cell>) {}

    fun makeMove(row: Int, col: Int) {
        // TODO: actual implementation instead of placeholder
        _highlightedCells.value = emptyList()

        var captured: Cell? = null
        if (abs(selected.row - row) > 1) {
            captured = Cell((selected.row + row) / 2, (selected.col + col) / 2)
        }
        movePiece(Move(selected.row, selected.col, row, col), captured)

        selected = Cell(-1, -1)
        println("Moved to ($row, $col)")
    }

    private fun movePiece(move: Move, captured: Cell? = null) {
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

        println(newBoard[move.toRow])
        println(newBoard[move.fromRow])

        captured?.let {
            val capturedRow = newBoard[captured.row].toMutableList()
            capturedRow[captured.col] = captured
            newBoard[captured.row] = capturedRow.toList()
        }

        _board.value = newBoard.toList()
        println(_board.value)
    }

    private fun checkSkipClick(row: Int, col: Int): Boolean {
        return ((row == selected.row && col == selected.col) || _board.value[row][col].piece!!.color != color || _currentPlayer.value != color)
    }

    private fun checkIfUpgrade(pieceCell: Cell): Boolean {
        val piece = pieceCell.piece ?: return false
        return when (piece.color to piece.type) {
            PlayerColor.BLACK to PieceType.PAWN -> pieceCell.row == 7
            PlayerColor.WHITE to PieceType.PAWN -> pieceCell.row == 0
            else -> false
        }
    }
}