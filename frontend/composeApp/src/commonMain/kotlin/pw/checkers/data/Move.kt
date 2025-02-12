package pw.checkers.data

import kotlinx.serialization.Serializable

@Serializable
data class Move(
    val fromRow: Int,
    val fromColumn: Int,
    val toRow: Int,
    val toColumn: Int,
)
