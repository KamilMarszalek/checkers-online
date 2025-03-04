package pw.checkers.game.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ResultDto {
    @SerialName("draw") DRAW,
    @SerialName("white") WHITE,
    @SerialName("black") BLACK
}