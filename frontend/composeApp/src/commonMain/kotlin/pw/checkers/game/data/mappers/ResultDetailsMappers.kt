package pw.checkers.game.data.mappers

import pw.checkers.game.data.dto.ResultDetailsDto
import pw.checkers.game.domain.model.ResultDetails

fun ResultDetailsDto.toDomain() = when(this) {
    ResultDetailsDto.NO_PIECES -> ResultDetails.NO_PIECES
    ResultDetailsDto.NO_MOVES -> ResultDetails.NO_MOVES
    ResultDetailsDto.FIFTY_MOVE -> ResultDetails.FIFTY_MOVE
    ResultDetailsDto.THREEFOLD_REPETITION -> ResultDetails.THREEFOLD_REPETITION
    ResultDetailsDto.RESIGN -> ResultDetails.RESIGN
}

fun ResultDetails.toDto() = when(this) {
    ResultDetails.NO_PIECES -> ResultDetailsDto.NO_PIECES
    ResultDetails.NO_MOVES -> ResultDetailsDto.NO_MOVES
    ResultDetails.FIFTY_MOVE -> ResultDetailsDto.FIFTY_MOVE
    ResultDetails.THREEFOLD_REPETITION -> ResultDetailsDto.THREEFOLD_REPETITION
    ResultDetails.RESIGN -> ResultDetailsDto.RESIGN
}