package pw.checkers.di

import io.ktor.client.*
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import pw.checkers.client.KtorRealtimeMessageClient
import pw.checkers.client.RealtimeMessageClient
import pw.checkers.data.response.GameCreated
import pw.checkers.viewModel.GameViewModel
import pw.checkers.viewModel.LoginViewModel

val appModule = module {
    single { HttpClient() }

    singleOf(::KtorRealtimeMessageClient) bind RealtimeMessageClient::class

    viewModelOf(::LoginViewModel)

    viewModel { parameters -> GameViewModel(gameCreated = parameters.get(), get()) }
}