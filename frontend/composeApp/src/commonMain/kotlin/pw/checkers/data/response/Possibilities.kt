package pw.checkers.data.response

import kotlinx.serialization.Serializable
import pw.checkers.data.Content
import pw.checkers.data.domain.Cell

@Serializable
data class Possibilities(
    val moves: List<Cell>,
) : Content
