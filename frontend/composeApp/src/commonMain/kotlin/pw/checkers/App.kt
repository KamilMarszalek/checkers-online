package pw.checkers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import pw.checkers.app.Checkers
import pw.checkers.di.gameModule
import pw.checkers.core.presentation.AppTheme

@Composable
fun App() {

    remember {
        startKoin {
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Checkers()
            }
        }
    }
}

