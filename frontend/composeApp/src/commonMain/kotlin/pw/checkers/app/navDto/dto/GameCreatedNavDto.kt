package pw.checkers.app.navDto.dto

import kotlinx.serialization.Serializable
import pw.checkers.game.domain.model.PlayerColor

@Serializable
data class GameCreatedNavDto(
    val gameId: String,
    val color: PlayerColor,
    val opponent: UserNavDto,
)
