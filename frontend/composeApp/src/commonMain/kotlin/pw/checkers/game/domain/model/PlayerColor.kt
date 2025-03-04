package pw.checkers.game.domain.model

enum class PlayerColor() {
    WHITE,
    BLACK
}

fun PlayerColor.toResult(): Result = when(this) {
    PlayerColor.WHITE -> Result.WHITE
    PlayerColor.BLACK -> Result.BLACK
}