package pw.checkers.data.response

import kotlinx.serialization.Serializable

@Serializable
data class GameEnd(
    val result: String
)
