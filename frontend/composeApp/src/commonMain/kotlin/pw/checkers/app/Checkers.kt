package pw.checkers.app

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import androidx.navigation.toRoute
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pw.checkers.app.navDto.dto.GameCreatedNavDto
import pw.checkers.app.navDto.dto.JoinedQueueNavDto
import pw.checkers.app.navDto.dto.UserNavDto
import pw.checkers.app.navDto.mappers.toDomain
import pw.checkers.app.navDto.mappers.toNavDto
import pw.checkers.game.domain.GameEvent
import pw.checkers.game.presentation.gameScreen.ui.GameScreen
import pw.checkers.app.navDto.serializableNavType
import pw.checkers.game.presentation.loginScreen.ui.LoginScreen
import pw.checkers.game.presentation.waitingScreen.ui.WaitingScreen
import pw.checkers.core.util.DoNothing
import kotlin.reflect.typeOf

@Composable
fun Checkers(navController: NavHostController) {

    NavHost(
        navController = navController,
        startDestination = Route.LoginScreen,
    ) {
        composable<Route.LoginScreen> {
            LoginScreen(
                loginViewModel = koinViewModel(),
                toWaiting = { joinedQueue, user ->
                    navController.navigate(route = Route.WaitingScreen(joinedQueue.toNavDto(), user.toNavDto()))
                },
                toGame = { gameCreated, user ->
                    navController.navigate(route = Route.GameScreen(gameCreated.toNavDto(), user.toNavDto()))
                }
            )
        }

        composable<Route.WaitingScreen>(
            typeMap = mapOf(
                typeOf<JoinedQueueNavDto>() to serializableNavType<JoinedQueueNavDto>(),
                typeOf<UserNavDto>() to serializableNavType<UserNavDto>()
            )
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<Route.WaitingScreen>()
            WaitingScreen(
                waitingViewModel = koinViewModel { parametersOf(route.joinedQueue.toDomain(), route.user.toDomain()) },
                toGame = { gameCreated, user ->
                    navController.popAndNavigate(Route.GameScreen(gameCreated.toNavDto(), user.toNavDto()))
                })
        }

        composable<Route.GameScreen>(
            typeMap = mapOf(
                typeOf<GameCreatedNavDto>() to serializableNavType<GameCreatedNavDto>(),
                typeOf<UserNavDto>() to serializableNavType<UserNavDto>()
            )
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<Route.GameScreen>()
            GameScreen(
                gameViewModel = koinViewModel { parametersOf(route.gameInfo.toDomain(), route.user.toDomain()) },
                backToMain = {
                    navController.navigate(
                        route = Route.LoginScreen, navOptions = navOptions {
                            popUpTo<Route.LoginScreen> { saveState = false }
                        })
                },
                nextGame = { event, user ->
                    when (event) {
                        is GameEvent.GameCreated -> navController.popAndNavigate(
                            Route.GameScreen(
                                event.toNavDto(),
                                user.toNavDto()
                            )
                        )

                        is GameEvent.JoinedQueue -> navController.popAndNavigate(
                            Route.WaitingScreen(
                                event.toNavDto(),
                                user.toNavDto()
                            )
                        )

                        else -> DoNothing
                    }
                }
            )
        }
    }
}

fun NavHostController.popAndNavigate(destRoute: Route) {
    navigateUp()
    navigate(route = destRoute)
}