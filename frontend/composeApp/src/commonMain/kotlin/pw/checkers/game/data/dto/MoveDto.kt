package pw.checkers.game.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class MoveDto(
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int,
)