package pw.checkers.di

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import pw.checkers.client.KtorRealtimeMessageClient
import pw.checkers.client.RealtimeMessageClient
import pw.checkers.viewModel.gameScreen.GameViewModel
import pw.checkers.viewModel.loginScreen.LoginViewModel
import pw.checkers.viewModel.waitingScreen.WaitingViewModel

val appModule = module {
    single { HttpClient { install(WebSockets) } }

    single { KtorRealtimeMessageClient(get()) }.bind<RealtimeMessageClient>()

    viewModel { LoginViewModel(get()) }

    viewModel { parameters -> WaitingViewModel(message = parameters.get(), get())}

    viewModel { parameters -> GameViewModel(gameInfo = parameters.get(), get()) }
}