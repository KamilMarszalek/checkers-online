package pw.checkers.game.data.mappers

import pw.checkers.game.data.dto.CellDto
import pw.checkers.game.domain.model.Cell

fun CellDto.toDomain() = Cell(row, col)

fun Cell.toDto() = CellDto(row, col)