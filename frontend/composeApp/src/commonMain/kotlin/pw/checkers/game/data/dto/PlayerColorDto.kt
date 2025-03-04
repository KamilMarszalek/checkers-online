package pw.checkers.game.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PlayerColorDto() {
    @SerialName("white") WHITE,
    @SerialName("black") BLACK
}