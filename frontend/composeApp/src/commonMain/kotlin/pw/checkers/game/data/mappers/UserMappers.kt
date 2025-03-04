package pw.checkers.game.data.mappers

import pw.checkers.game.data.dto.UserDto
import pw.checkers.game.domain.model.User

fun UserDto.toDomain() = User(username)

fun User.toDto() = UserDto(username)