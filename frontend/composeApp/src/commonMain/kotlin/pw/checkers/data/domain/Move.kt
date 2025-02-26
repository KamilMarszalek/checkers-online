package pw.checkers.data.domain

import kotlinx.serialization.Serializable

@Serializable
data class Move(
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int,
)
