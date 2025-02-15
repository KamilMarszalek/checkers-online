package pw.checkers

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pw.checkers.data.response.GameCreated
import pw.checkers.navigation.GameScreen
import pw.checkers.navigation.LoginScreen
import pw.checkers.ui.screens.GameScreen
import pw.checkers.ui.screens.LoginScreen

@Composable
fun Checkers(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = LoginScreen,
        modifier = Modifier.fillMaxSize(),

        ) {
        composable<LoginScreen> {
            LoginScreen(
                koinViewModel(),
                onLoginClick = { gameCreated ->
                    navController.navigate(GameScreen(gameCreated))
                }
            )
        }

        composable<GameScreen> { backStackEntry ->
            val gameCreated: GameCreated = backStackEntry.toRoute()
            GameScreen(
                gameViewModel = koinViewModel(parameters = { parametersOf(gameCreated) })
            )
        }
    }
}