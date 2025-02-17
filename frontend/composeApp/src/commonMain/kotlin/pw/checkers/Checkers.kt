package pw.checkers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pw.checkers.data.response.GameCreated
import pw.checkers.ui.screens.GameScreen
import pw.checkers.ui.screens.LoginScreen

@Composable
fun Checkers(navController: NavHostController = rememberNavController()) {

    NavHost(
        navController = navController,
        startDestination = Routes.Login,
//        modifier = Modifier.fillMaxSize(),
    ) {
        composable<Routes.Login> {
            LoginScreen(
                koinViewModel(),
                onLoginClick = { gameCreated ->
                    navController.navigate(Routes.Game(Json.encodeToString(gameCreated)))
                }
            )
        }

        composable<Routes.Game> { backStackEntry ->
            val game = remember { requireNotNull(backStackEntry.toRoute<Routes.Game>()) }
            val gameCreated = remember(game) { Json.decodeFromString<GameCreated>(game.encodedGameCreated) }
            GameScreen(
                gameViewModel = koinViewModel(parameters = { parametersOf(gameCreated) })
            )
        }
    }
}


sealed class Routes {
    @Serializable
    data object Login: Routes()

    @Serializable
    data class Game(val encodedGameCreated: String): Routes()
}