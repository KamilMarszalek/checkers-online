package pw.checkers.game.data.mappers

import pw.checkers.game.data.dto.PlayerColorDto
import pw.checkers.game.domain.model.PlayerColor


fun PlayerColorDto.toDomain() = when(this) {
    PlayerColorDto.WHITE -> PlayerColor.WHITE
    PlayerColorDto.BLACK -> PlayerColor.BLACK
}

fun PlayerColor.toDto() = when(this) {
    PlayerColor.WHITE -> PlayerColorDto.WHITE
    PlayerColor.BLACK -> PlayerColorDto.BLACK
}