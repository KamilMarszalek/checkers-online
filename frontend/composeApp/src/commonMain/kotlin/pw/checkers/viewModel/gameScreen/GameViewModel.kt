package pw.checkers.viewModel.gameScreen

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pw.checkers.client.RealtimeMessageClient
import pw.checkers.data.domain.*
import pw.checkers.data.message.Message
import pw.checkers.data.messageType.MessageType
import pw.checkers.data.request.GetPossibilities
import pw.checkers.data.request.JoinQueue
import pw.checkers.data.request.MakeMove
import pw.checkers.data.response.GameEnd
import pw.checkers.data.response.GameInfo
import pw.checkers.data.response.MoveInfo
import pw.checkers.data.response.Possibilities
import pw.checkers.models.createInitialBoard
import pw.checkers.viewModel.BaseViewModel

class GameViewModel(
    gameInfo: GameInfo, val user: User, messageClient: RealtimeMessageClient
) : BaseViewModel(messageClient) {

    private val color = gameInfo.color
    private val gameId = gameInfo.gameId
    private val opponent = gameInfo.opponent
    private var selected = Cell(-1, -1)
    private var multiMove: Boolean = false

    private val _state = MutableStateFlow(GameState())
    val state = _state.asStateFlow()

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

    private val _events = MutableSharedFlow<GameEvent>()
    val events = _events.asSharedFlow()


    @Suppress("UNUSED_PARAMETER")
    private fun unselectPiece(row: Int, col: Int) {
        if (multiMove) {
            return
        }
        selected = Cell(-1, -1)
        _state.update { it.copy(highlightedCells = emptyList()) }
    }

    private fun getPossibleMoves(row: Int, col: Int) {
        if (checkSkipClick(row, col)) {
            return
        }

        selected = Cell(row, col)
        sendMessage(MessageType.POSSIBILITIES, GetPossibilities(gameId, row, col))
    }

    private fun makeMove(row: Int, col: Int) {
        _state.update { it.copy(highlightedCells = emptyList()) }
        val move = Move(selected.row, selected.col, row, col)
        sendMessage(MessageType.MOVE, MakeMove(gameId, move))
    }

    private fun setHighlighted(cells: List<Cell>) {
        _state.update { it.copy(highlightedCells = cells) }
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

        _board.update { newBoard.toList() }
    }

    private fun checkSkipClick(row: Int, col: Int): Boolean {
        return ((row == selected.row && col == selected.col) || _board.value[row][col].piece!!.color != color || _state.value.currentPlayer != color || multiMove)
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
            MessageType.MOVE -> handleMessageContent(msg, ::processMoveInfoMessage)
            MessageType.POSSIBILITIES -> handleMessageContent(msg, ::processPossibilities)
            MessageType.GAME_ENDING -> handleMessageContent(msg, ::processGameEnd)
            MessageType.WAITING, MessageType.GAME_CREATED -> processNextGameMessages(msg)
            MessageType.REMATCH_REQUEST -> processRematchRequest()
            MessageType.REJECTION -> processRematchRejection()
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
        _state.update { it.copy(currentPlayer = moveInfo.currentTurn) }
        multiMove = moveInfo.hasMoreTakes
    }

    private fun processPossibilities(possibilities: Possibilities) {
        setHighlighted(possibilities.moves)
    }

    private fun processGameEnd(gameEnd: GameEnd) {
        _state.update {
            it.copy(gameEnded = true, result = gameEnd.result)
        }
    }

    private fun processNextGameMessages(message: Message) {
        if (!_state.value.gameEnded) return
        viewModelScope.launch {
            _events.emit(GameEvent.NextGame(message))
        }
    }

    fun getEndGameText(): String {
        val result = _state.value.result
        return when {
            result == color.toResult() -> "You won"
            result != Result.DRAW -> "${opponent.username} won"
            else -> "Draw"
        }
    }

    fun getRematchRequestMessage() = "${opponent.username} requested a rematch"

    private fun playNextGame() {
        sendMessage(MessageType.JOIN_QUEUE, JoinQueue(user))
    }

    private fun acceptRematch() {
        sendMessage(MessageType.ACCEPT_REMATCH, gameId.toDataClass())
    }

    private fun requestRematch() {
        sendMessage(MessageType.REMATCH_REQUEST, gameId.toDataClass())
        _state.update {
            it.copy(
                rematchPending = true,
                rematchRequested = false,
                rematchRequestRejected = false,
                rematchPropositionRejected = false,
            )
        }
    }

    private fun declineRematch() {
        sendMessage(MessageType.DECLINE_REMATCH, gameId.toDataClass())
        _state.update {
            it.copy(
                rematchPending = false,
                rematchRequested = false,
                rematchRequestRejected = true,
                rematchPropositionRejected = false,
            )
        }
    }

    private fun processRematchRequest() {
        _state.update {
            it.copy(
                rematchPending = false,
                rematchRequested = true,
                rematchRequestRejected = false,
                rematchPropositionRejected = false,
            )
        }
    }

    private fun processRematchRejection() {
        _state.update {
            it.copy(
                rematchPending = false,
                rematchRequested = false,
                rematchRequestRejected = false,
                rematchPropositionRejected = true,
            )
        }
    }

    fun onAction(action: GameAction) {
        when (action) {
            is GameAction.GetPossibleMoves -> getPossibleMoves(action.row, action.col)
            is GameAction.MakeMove -> makeMove(action.row, action.col)
            is GameAction.UnselectPiece -> unselectPiece(action.row, action.col)
            GameAction.AcceptRematch -> acceptRematch()
            GameAction.DeclineRematch -> declineRematch()
            GameAction.PlayNext -> playNextGame()
            GameAction.RequestRematch -> requestRematch()
            GameAction.MainMenu -> {}
        }
    }
}