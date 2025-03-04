package pw.checkers.game.domain.model

enum class PieceType() {
    PAWN, QUEEN
}

data class Piece(val color: PlayerColor, val type: PieceType)

