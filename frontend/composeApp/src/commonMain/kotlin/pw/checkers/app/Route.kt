package pw.checkers.app

import kotlinx.serialization.Serializable
import pw.checkers.app.navDto.dto.GameCreatedNavDto
import pw.checkers.app.navDto.dto.JoinedQueueNavDto
import pw.checkers.app.navDto.dto.UserNavDto

sealed interface Route {
    @Serializable
    data object LoginScreen : Route

    @Serializable
    data class WaitingScreen(val joinedQueue: JoinedQueueNavDto, val user: UserNavDto) : Route

    @Serializable
    data class GameScreen(val gameInfo: GameCreatedNavDto, val user: UserNavDto) : Route
}
