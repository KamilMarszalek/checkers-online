package pw.checkers.di

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import pw.checkers.client.KtorRealtimeMessageClient
import pw.checkers.client.RealtimeMessageClient
import pw.checkers.viewModel.gameScreen.GameViewModel
import pw.checkers.viewModel.loginScreen.LoginViewModel

val appModule = module {
    single { HttpClient { install(WebSockets) } }

    single { KtorRealtimeMessageClient(get()) }.bind<RealtimeMessageClient>()

    viewModel { LoginViewModel(get()) }

    viewModel { parameters -> GameViewModel(gameCreated = parameters.get(), get()) }
}