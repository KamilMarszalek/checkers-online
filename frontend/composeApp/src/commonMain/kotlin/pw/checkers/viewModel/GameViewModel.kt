package pw.checkers.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import pw.checkers.client.RealtimeMessageClient
import pw.checkers.data.Content
import pw.checkers.data.domain.Move
import pw.checkers.data.domain.Cell
import pw.checkers.data.domain.PieceType
import pw.checkers.models.createInitialBoard
import pw.checkers.data.domain.PlayerColor
import pw.checkers.data.message.Message
import pw.checkers.data.messageType.MessageType
import pw.checkers.data.request.GetPossibilities
import pw.checkers.data.request.MakeMove
import pw.checkers.data.response.GameCreated
import pw.checkers.data.response.GameEnd
import pw.checkers.data.response.MoveInfo
import pw.checkers.data.response.Possibilities
import pw.checkers.util.handleMessageContent
import kotlin.math.abs

class GameViewModel(gameCreated: GameCreated, private val messageClient: RealtimeMessageClient) : ViewModel() {

    private val color = gameCreated.color
    private val gameId = gameCreated.gameId
    private val opponent = gameCreated.opponent

    init {
        viewModelScope.launch {
            messageClient.getMessageStream().collect { message ->
                handleMessage(message)
            }
        }
    }

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
        if (checkSkipClick(row, col)) {
            return
        }

        selected = Cell(row, col)
        sendMessage(MessageType.POSSIBILITIES, GetPossibilities(gameId, row, col))

        println("clicked ($row, $col)")
    }

    fun makeMove(row: Int, col: Int) {
        _highlightedCells.value = emptyList()
        val move = Move(selected.row, selected.col, row, col)

        sendMessage(MessageType.MOVE, MakeMove(gameId, move))

        var captured: Cell? = null
        if (abs(selected.row - row) > 1) {
            captured = Cell((selected.row + row) / 2, (selected.col + col) / 2)
        }
        movePiece(move, captured)

        selected = Cell(-1, -1)
        println("Moved to ($row, $col)")
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

    private fun handleMessage(msg: Message) {
        println(msg)
        when (msg.type) {
            MessageType.MOVE -> handleMessageContent<MoveInfo>(msg, ::processMoveInfoMessage)
            MessageType.POSSIBILITIES -> handleMessageContent<Possibilities>(msg, ::processPossibilities)
            MessageType.GAME_ENDING -> handleMessageContent<GameEnd>(msg, ::processGameEnd)
            else -> return
        }
    }

    private fun processMoveInfoMessage(moveInfo: MoveInfo) {
        if (moveInfo.previousTurn != _currentPlayer.value) {
            movePiece(moveInfo.move, moveInfo.capturedPiece)
            _currentPlayer.value = moveInfo.currentTurn
        }

    }

    private fun processPossibilities(possibilities: Possibilities) {
        setHighlighted(possibilities.moves)
    }

    private fun processGameEnd(gameEnd: GameEnd) {
        println(gameEnd)
    }

    private inline fun <reified T: Content> sendMessage(type: MessageType, content: T) {
        val message = Message(
            type = type,
            content = Json.encodeToJsonElement<T>(content)
        )

        viewModelScope.launch {
            println(message)
            messageClient.sendMessage(message)
        }
    }
}