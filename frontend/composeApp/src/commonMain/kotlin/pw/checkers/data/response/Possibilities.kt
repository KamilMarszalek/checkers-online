package pw.checkers.data.response

import kotlinx.serialization.Serializable
import pw.checkers.data.Cell

@Serializable
data class Possibilities(
    val moves: List<Cell>,
)
