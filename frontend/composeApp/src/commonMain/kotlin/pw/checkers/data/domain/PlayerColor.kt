package pw.checkers.data.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PlayerColor() {
    @SerialName("white")
    WHITE,

    @SerialName("black")
    BLACK
}

fun PlayerColor.toResult(): Result = when(this) {
    PlayerColor.WHITE -> Result.WHITE
    PlayerColor.BLACK -> Result.BLACK
}