package pw.checkers.game.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import pw.checkers.game.presentation.BaseViewModel

@Composable
fun messageCollectionDisposableEffect(viewModel: BaseViewModel) {
    DisposableEffect(Unit) {
        viewModel.startCollecting()

        onDispose {
            viewModel.stopCollecting()
        }
    }
}