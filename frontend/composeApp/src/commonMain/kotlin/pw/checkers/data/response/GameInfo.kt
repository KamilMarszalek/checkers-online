package pw.checkers.data.response

import kotlinx.serialization.Serializable
import pw.checkers.data.Content
import pw.checkers.data.domain.PlayerColor
import pw.checkers.data.domain.User

@Serializable
data class GameInfo(
    val gameId: String,
    val color: PlayerColor,
    val opponent: User
) : Content
