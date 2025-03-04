package pw.checkers.di

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import pw.checkers.game.data.client.KtorRealtimeMessageClient
import pw.checkers.game.data.client.RealtimeMessageClient
import pw.checkers.game.data.repository.GameRepositoryImpl
import pw.checkers.game.domain.repository.GameRepository
import pw.checkers.game.presentation.gameScreen.GameViewModel
import pw.checkers.game.presentation.loginScreen.LoginViewModel
import pw.checkers.game.presentation.waitingScreen.WaitingViewModel


val gameModule = module {
    single { HttpClient { install(WebSockets) } }

    single { KtorRealtimeMessageClient(get()) }.bind<RealtimeMessageClient>()

    single { GameRepositoryImpl(get()) }.bind<GameRepository>()

    viewModel { LoginViewModel(get()) }

    viewModel { parameters -> WaitingViewModel(joinedQueue = parameters.get(), parameters.get(), get()) }

    viewModel { parameters -> GameViewModel(gameInfo = parameters.get(), parameters.get(), get()) }
}