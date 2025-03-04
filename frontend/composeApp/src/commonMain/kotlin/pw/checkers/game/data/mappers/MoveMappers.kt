package pw.checkers.game.data.mappers

import pw.checkers.game.data.dto.MoveDto
import pw.checkers.game.domain.model.Move

fun MoveDto.toDomain() = Move(fromRow, fromCol, toRow, toCol)

fun Move.toDto() = MoveDto(fromRow, fromCol, toRow, toCol)