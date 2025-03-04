package pw.checkers.game.domain.model

data class Move(
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int,
)
