package pw.checkers.data.domain

import kotlinx.serialization.Serializable

@Serializable
data class Cell(
    val row: Int,
    val col: Int,
    val piece: Piece? = null,
)
