package pw.checkers.viewModel.gameScreen

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import pw.checkers.client.RealtimeMessageClient
import pw.checkers.data.domain.*
import pw.checkers.data.message.Message
import pw.checkers.data.messageType.MessageType
import pw.checkers.data.request.GetPossibilities
import pw.checkers.data.request.MakeMove
import pw.checkers.data.response.GameInfo
import pw.checkers.data.response.GameEnd
import pw.checkers.data.response.MoveInfo
import pw.checkers.data.response.Possibilities
import pw.checkers.models.createInitialBoard
import pw.checkers.viewModel.BaseViewModel

class GameViewModel(
    gameInfo: GameInfo,
    messageClient: RealtimeMessageClient
) : BaseViewModel(messageClient) {

    private val color = gameInfo.color
    private val gameId = gameInfo.gameId
    private val opponent = gameInfo.opponent

    private val _uiState = MutableStateFlow<GameScreenState>(GameScreenState.Game)
    val uiState = _uiState.asStateFlow()


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
    private var multiMove: Boolean = false

    @Suppress("UNUSED_PARAMETER")
    fun unselectPiece(row: Int, col: Int) {
        if (multiMove) {
            return
        }
        selected = Cell(-1, -1)
        _highlightedCells.value = emptyList()
    }

    fun getPossibleMoves(row: Int, col: Int) {
        if (checkSkipClick(row, col)) {
            return
        }

        selected = Cell(row, col)
        sendMessage(MessageType.POSSIBILITIES, GetPossibilities(gameId, row, col))
    }

    fun makeMove(row: Int, col: Int) {
        _highlightedCells.value = emptyList()
        val move = Move(selected.row, selected.col, row, col)
        sendMessage(MessageType.MOVE, MakeMove(gameId, move))
    }

    private fun setHighlighted(cells: List<Cell>) {
        _highlightedCells.value = cells
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

        captured?.let {
            val capturedRow = newBoard[captured.row].toMutableList()
            capturedRow[captured.col] = captured
            newBoard[captured.row] = capturedRow.toList()
        }

        _board.value = newBoard.toList()
        println(move)
    }

    private fun checkSkipClick(row: Int, col: Int): Boolean {
        return ((row == selected.row && col == selected.col) || _board.value[row][col].piece!!.color != color || _currentPlayer.value != color || multiMove)
    }

    private fun checkIfUpgrade(pieceCell: Cell): Boolean {
        val piece = pieceCell.piece ?: return false
        return when (piece.color to piece.type) {
            PlayerColor.BLACK to PieceType.PAWN -> pieceCell.row == 7
            PlayerColor.WHITE to PieceType.PAWN -> pieceCell.row == 0
            else -> false
        }
    }

    override fun handleServerMessage(msg: Message) {
        println("Received: $msg")
        when (msg.type) {
            MessageType.MOVE -> handleMessageContent<MoveInfo>(msg, ::processMoveInfoMessage)
            MessageType.POSSIBILITIES -> handleMessageContent<Possibilities>(msg, ::processPossibilities)
            MessageType.GAME_ENDING -> handleMessageContent<GameEnd>(msg, ::processGameEnd)
            else -> return
        }
    }

    private fun processMoveInfoMessage(moveInfo: MoveInfo) {
        if (moveInfo.hasMoreTakes || color == moveInfo.currentTurn) {
            selected = Cell(moveInfo.move.toRow, moveInfo.move.toCol)
            multiMove = true
        } else {
            selected = Cell(-1, -1)
            multiMove = false
        }
        movePiece(moveInfo.move, moveInfo.capturedPiece)
        _currentPlayer.value = moveInfo.currentTurn
        multiMove = moveInfo.hasMoreTakes
    }

    private fun processPossibilities(possibilities: Possibilities) {
        setHighlighted(possibilities.moves)
    }

    private fun processGameEnd(gameEnd: GameEnd) {
        _uiState.value = GameScreenState.GameEnded(gameEnd.result)
    }

    fun getEndGameText(): String {
        val result = (_uiState.value as GameScreenState.GameEnded).result
        return when {
            result == color.toResult() -> "You won"
            result != Result.DRAW -> "${opponent.username} won"
            else -> "Draw"
        }
    }
}