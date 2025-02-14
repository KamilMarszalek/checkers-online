package pw.checkers.data.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Result {
    @SerialName("draw")
    DRAW,

    @SerialName("white")
    WHITE,

    @SerialName("black")
    BLACK,
}