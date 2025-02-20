package pw.checkers

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pw.checkers.data.message.Message
import pw.checkers.data.response.GameInfo
import pw.checkers.navigation.serializableNavType
import pw.checkers.ui.screens.GameScreen
import pw.checkers.ui.screens.LoginScreen
import pw.checkers.ui.screens.WaitingScreen
import kotlin.reflect.typeOf

@Composable
fun Checkers(navController: NavHostController = rememberNavController()) {

    NavHost(
        navController = navController,
        startDestination = Routes.LoginScreen,
    ) {
        composable<Routes.LoginScreen> {
            LoginScreen(
                loginViewModel = koinViewModel(),
                onPlayClick = { message ->
                    navController.navigate(route = Routes.WaitingScreen(message))
                }
            )
        }

        composable<Routes.WaitingScreen>(
            typeMap = mapOf(typeOf<Message>() to serializableNavType<Message>())
        ) { backStackEntry ->
            val message = backStackEntry.toRoute<Routes.WaitingScreen>().message
            WaitingScreen(
                waitingViewModel = koinViewModel { parametersOf(message) },
                onGameCreated = { gameInfo -> navController.navigate(Routes.GameScreen(gameInfo)) })
        }

        composable<Routes.GameScreen>(
            typeMap = mapOf(typeOf<GameInfo>() to serializableNavType<GameInfo>())
        ) { backStackEntry ->
            val gameInfo = backStackEntry.toRoute<Routes.GameScreen>().gameInfo
            GameScreen(
                gameViewModel = koinViewModel { parametersOf(gameInfo) })
        }
    }
}


sealed interface Routes {
    @Serializable
    data object LoginScreen : Routes

    @Serializable
    data class WaitingScreen(val message: Message) : Routes

    @Serializable
    data class GameScreen(val gameInfo: GameInfo) : Routes
}