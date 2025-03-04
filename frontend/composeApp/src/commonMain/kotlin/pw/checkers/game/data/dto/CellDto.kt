package pw.checkers.game.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CellDto (
    val row: Int,
    val col: Int,
)
