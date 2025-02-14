package pw.checkers.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PlayerColor() {
    @SerialName("white")
    WHITE,

    @SerialName("black")
    BLACK
}
