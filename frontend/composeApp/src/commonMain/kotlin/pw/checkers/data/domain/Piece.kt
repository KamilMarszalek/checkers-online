package pw.checkers.data.domain

import kotlinx.serialization.Serializable

@Serializable
enum class PieceType() {
    PAWN, QUEEN
}

@Serializable
data class Piece(val color: PlayerColor, val type: PieceType)

