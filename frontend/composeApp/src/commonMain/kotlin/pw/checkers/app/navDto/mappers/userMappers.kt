package pw.checkers.app.navDto.mappers

import pw.checkers.app.navDto.dto.UserNavDto
import pw.checkers.game.domain.model.User

fun User.toNavDto() = UserNavDto(username)

fun UserNavDto.toDomain() = User(username)