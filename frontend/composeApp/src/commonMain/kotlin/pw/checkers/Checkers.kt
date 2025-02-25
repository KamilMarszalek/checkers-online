package pw.checkers

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pw.checkers.data.domain.User
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
                loginViewModel = koinViewModel(), onPlayClick = { message, user ->
                    navController.navigate(route = Routes.WaitingScreen(message, user))
                })
        }

        composable<Routes.WaitingScreen>(
            typeMap = mapOf(
                typeOf<Message>() to serializableNavType<Message>(), typeOf<User>() to serializableNavType<User>()
            )
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.WaitingScreen>()
            WaitingScreen(
                waitingViewModel = koinViewModel { parametersOf(route.message, route.user) },
                onGameCreated = { gameInfo, user ->
                    navController.popAndNavigate(Routes.GameScreen(gameInfo, user))
                })
        }

        composable<Routes.GameScreen>(
            typeMap = mapOf(
                typeOf<GameInfo>() to serializableNavType<GameInfo>(), typeOf<User>() to serializableNavType<User>()
            )
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<Routes.GameScreen>()
            GameScreen(
                gameViewModel = koinViewModel { parametersOf(route.gameInfo, route.user) },
                onMainMenuClick = {
                    navController.navigate(
                        route = Routes.LoginScreen, navOptions = navOptions {
                            popUpTo<Routes.LoginScreen> { saveState = false }
                        })
                },
                nextGame = { message, user ->
                    navController.popAndNavigate(Routes.WaitingScreen(message, user))
                }
            )
        }
    }
}


sealed interface Routes {
    @Serializable
    data object LoginScreen : Routes

    @Serializable
    data class WaitingScreen(val message: Message, val user: User) : Routes

    @Serializable
    data class GameScreen(val gameInfo: GameInfo, val user: User) : Routes
}

fun NavHostController.popAndNavigate(destRoute: Routes) {
    navigateUp()
    navigate(route = destRoute)
}