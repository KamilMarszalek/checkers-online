package pw.checkers

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.KoinContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import pw.checkers.di.appModule
import pw.checkers.ui.theme.AppTheme

@Composable
fun App() {

    remember {
        startKoin {
            modules(appModule)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stopKoin()
        }
    }

    AppTheme {
        Scaffold {
            KoinContext {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Checkers()
                }
            }
        }
    }
}

