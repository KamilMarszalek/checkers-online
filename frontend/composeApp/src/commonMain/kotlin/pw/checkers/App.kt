package pw.checkers

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import pw.checkers.app.Checkers
import pw.checkers.core.presentation.AppTheme
import pw.checkers.di.gameModule

@Composable
fun App(serverAddress: String = "127.0.0.1:8080") {

    remember {
        startKoin {
            properties(mapOf("serverAddress" to serverAddress))
            modules(gameModule)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stopKoin()
        }
    }

    AppTheme {
        Scaffold {
            Checkers()
        }
    }
}

