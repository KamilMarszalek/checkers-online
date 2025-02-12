package pw.checkers.data.response

import kotlinx.serialization.Serializable

@Serializable
data class GameCreated(
    val gameID: String,
    val assignedColor: String,
)
