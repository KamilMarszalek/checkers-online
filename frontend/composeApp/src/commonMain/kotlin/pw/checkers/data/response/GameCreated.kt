package pw.checkers.data.response

import kotlinx.serialization.Serializable
import pw.checkers.data.PlayerColor
import pw.checkers.data.User

@Serializable
data class GameCreated(
    val gameID: String,
    val assignedColor: PlayerColor,
    val opponent: User
)
