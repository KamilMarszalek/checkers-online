package pw.checkers.game.data.mappers

import pw.checkers.game.data.dto.ResultDto
import pw.checkers.game.domain.model.Result

fun ResultDto.toDomain() = when(this) {
    ResultDto.DRAW -> Result.DRAW
    ResultDto.WHITE -> Result.WHITE
    ResultDto.BLACK -> Result.BLACK
}

fun Result.toDto() = when(this) {
    Result.DRAW -> ResultDto.DRAW
    Result.WHITE -> ResultDto.WHITE
    Result.BLACK -> ResultDto.BLACK
}