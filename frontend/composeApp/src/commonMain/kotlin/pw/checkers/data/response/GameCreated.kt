package pw.checkers.data.response

import kotlinx.serialization.Serializable
import pw.checkers.data.domain.PlayerColor
import pw.checkers.data.domain.User

@Serializable
data class GameCreated(
    val gameID: String,
    val assignedColor: PlayerColor,
    val opponent: User
)
