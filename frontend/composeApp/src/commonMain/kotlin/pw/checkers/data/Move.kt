package pw.checkers.data

import kotlinx.serialization.Serializable

@Serializable
data class Move(
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int,
)
