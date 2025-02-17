package pw.checkers.data.response

import kotlinx.serialization.Serializable
import pw.checkers.data.Content
import pw.checkers.data.domain.Result

@Serializable
data class GameEnd(
    val result: Result
) : Content
