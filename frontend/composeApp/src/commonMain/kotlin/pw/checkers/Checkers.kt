package pw.checkers

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
        startDestination = "login",
        modifier = Modifier.fillMaxSize(),
    ) {
        composable("login") {
            LoginScreen(
                koinViewModel(),
                onLoginClick = { gameCreated ->
                    val gameDataJson = Json.encodeToString(gameCreated)
                    navController.navigate(route = "game?data=$gameDataJson")
                }
            )
        }

        composable(
            route = "game?data={data}",
            arguments = listOf(navArgument("data") { type = NavType.StringType })
        ) { backStackEntry ->
            val dataJson = backStackEntry.arguments!!.getString("data") ?: ""
            val gameCreated = Json.decodeFromString<GameCreated>(dataJson)
            GameScreen(
                gameViewModel = koinViewModel(parameters = { parametersOf(gameCreated) })
            )
        }
    }
}