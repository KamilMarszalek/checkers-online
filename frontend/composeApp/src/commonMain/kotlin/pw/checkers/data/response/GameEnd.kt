package pw.checkers.data.response

import kotlinx.serialization.Serializable
import pw.checkers.data.Result

@Serializable
data class GameEnd(
    val result: Result
)
