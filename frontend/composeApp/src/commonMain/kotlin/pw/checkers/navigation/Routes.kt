package pw.checkers.navigation

import kotlinx.serialization.Serializable
import pw.checkers.data.response.GameCreated

@Serializable
data object LoginScreen

@Serializable
data class GameScreen(val gameCreated: GameCreated)