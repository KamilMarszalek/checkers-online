package pw.checkers.data.domain

import kotlinx.serialization.Serializable
import pw.checkers.data.Content

@Serializable
value class GameId(val gameId: String) : Content

@Serializable
data class GameIdObject(val gameId: GameId) : Content

fun GameId.toDataClass() = GameIdObject(this)