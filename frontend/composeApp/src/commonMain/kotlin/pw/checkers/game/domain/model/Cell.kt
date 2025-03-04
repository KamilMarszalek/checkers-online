package pw.checkers.game.domain.model

data class Cell (
    val row: Int,
    val col: Int,
    val piece: Piece? = null,
)
