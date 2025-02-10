package pw.checkers.data.request

import kotlinx.serialization.Serializable

@Serializable
data class GetPossibilities(
    val gameId: String,
    val row: Int,
    val col: Int,
)
