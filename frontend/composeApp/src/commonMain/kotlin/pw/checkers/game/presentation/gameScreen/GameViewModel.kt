package pw.checkers.game.presentation.gameScreen

import androidx.lifecycle.viewModelScope
import checkers.composeapp.generated.resources.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pw.checkers.core.presentation.UiText
import pw.checkers.core.presentation.UiText.StringResourceId
import pw.checkers.game.domain.GameAction
import pw.checkers.game.domain.GameEvent
import pw.checkers.game.domain.model.*
import pw.checkers.game.domain.repository.GameRepository
import pw.checkers.game.presentation.BaseViewModel

class GameViewModel(
    gameInfo: GameEvent.GameCreated, val user: User, gameRepository: GameRepository
) : BaseViewModel(gameRepository) {

    val color = gameInfo.color
    private val gameId = gameInfo.gameId
    val opponent = gameInfo.opponent
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


    private fun unselectPiece() {
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
        sendAction(GameAction.GetPossibilities(gameId, row, col))
    }

    private fun makeMove(row: Int, col: Int) {
        if (_state.value.gameEnded) return

        _state.update { it.copy(highlightedCells = emptyList()) }
        val move = Move(selected.row, selected.col, row, col)
        sendAction(GameAction.MakeMove(gameId, move))
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
        return (
            (row == selected.row && col == selected.col) ||
            _board.value[row][col].piece!!.color != color ||
            _state.value.currentPlayer != color ||
            multiMove ||
            _state.value.gameEnded
        )
    }

    private fun checkIfUpgrade(pieceCell: Cell): Boolean {
        val piece = pieceCell.piece ?: return false
        return when (piece.color to piece.type) {
            PlayerColor.BLACK to PieceType.PAWN -> pieceCell.row == 7
            PlayerColor.WHITE to PieceType.PAWN -> pieceCell.row == 0
            else -> false
        }
    }

    override fun handleGameEvent(event: GameEvent) {
            when (event) {
            is GameEvent.MoveResult -> processMove(event)
            is GameEvent.PossibleMoves -> processPossibilities(event)
            is GameEvent.JoinedQueue, is GameEvent.GameCreated -> processNextGameMessages(event)
            is GameEvent.GameEnd -> processGameEnd(event)
            is GameEvent.RematchRequest -> processRematchRequest()
            is GameEvent.RematchRejected -> processRematchRejection()
        }
    }

    private fun processMove(moveInfo: GameEvent.MoveResult) {
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

    private fun processPossibilities(possibilities: GameEvent.PossibleMoves) {
        setHighlighted(possibilities.moves)
    }

    private fun processGameEnd(gameEnd: GameEvent.GameEnd) {
        _state.update {
            it.copy(gameEnded = true, result = gameEnd.result, resultDetails = gameEnd.details)
        }
    }

    private fun processNextGameMessages(event: GameEvent) {
        if (!_state.value.gameEnded) return
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    fun getEndGameText(): UiText {
        val result = _state.value.result
        return when {
            result == color.toResult() -> StringResourceId(Res.string.game_player_win_title)
            result != Result.DRAW -> StringResourceId(Res.string.game_opponent_win_title, arrayOf(opponent.username))
            else -> StringResourceId(Res.string.game_draw_title)
        }
    }

    fun getResultDetailsText(): UiText {
        val resultDetails = _state.value.resultDetails
        return when(resultDetails) {
            ResultDetails.NO_PIECES -> StringResourceId(Res.string.result_details_no_pieces)
            ResultDetails.NO_MOVES -> StringResourceId(Res.string.result_details_no_moves)
            ResultDetails.FIFTY_MOVE -> StringResourceId(Res.string.result_details_fifty_move)
            ResultDetails.THREEFOLD_REPETITION -> StringResourceId(Res.string.result_details_threefold)
            ResultDetails.RESIGN -> {
                val result = _state.value.result
                val loser = if (result == color.toResult()) opponent.username else user.username
                StringResourceId(Res.string.result_details_resign, arrayOf(loser))
            }
            null -> UiText.DynamicString("")
        }
    }

    fun getRematchRequestMessage() = StringResourceId(Res.string.rematch_requested_text, arrayOf(opponent.username))

    private fun playNextGame() {
        sendAction(GameAction.JoinQueue(user))
    }

    private fun leaveGame() {
        sendAction(GameAction.LeaveGame(gameId))
    }

    private fun resignGame() {
        _state.update { it.copy(gameEnded = true) }
        sendAction(GameAction.Resign(gameId))
    }

    private fun acceptRematch() {
        sendAction(GameAction.AcceptRematch(gameId))
    }

    private fun requestRematch() {
        sendAction(GameAction.RequestRematch(gameId))
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
        sendAction(GameAction.DeclineRematch(gameId))
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

    fun onAction(action: GameBoardAction) {
        when (action) {
            is GameBoardAction.OnPieceClick -> getPossibleMoves(action.row, action.col)
            is GameBoardAction.OnHighLightedClick -> makeMove(action.row, action.col)
            GameBoardAction.OnEmptyClick -> unselectPiece()

            GameBoardAction.OnMainMenuClick -> leaveGame()
            GameBoardAction.OnNextGameClick -> playNextGame()
            GameBoardAction.OnRematchRequestClick -> requestRematch()
            GameBoardAction.OnRematchAcceptClick -> acceptRematch()
            GameBoardAction.OnRematchDeclineClick -> declineRematch()
            GameBoardAction.OnResignClick -> resignGame()
        }
    }

    fun ignoreGameEndPopup() {
        _state.update { it.copy(ignorePopup = true) }
    }
}