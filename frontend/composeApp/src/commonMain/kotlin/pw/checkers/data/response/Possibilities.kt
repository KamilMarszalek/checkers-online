package pw.checkers.data.response

import kotlinx.serialization.Serializable
import pw.checkers.data.Move

@Serializable
data class Possibilities(
    val moves: List<Move>,
)
