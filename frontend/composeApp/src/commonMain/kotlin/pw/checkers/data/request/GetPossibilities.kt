package pw.checkers.data.request

import kotlinx.serialization.Serializable
import pw.checkers.data.Content

@Serializable
data class GetPossibilities(
    val gameId: String,
    val row: Int,
    val col: Int,
) : Content
