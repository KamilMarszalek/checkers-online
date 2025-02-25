package pw.checkers.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import pw.checkers.viewModel.BaseViewModel
import pw.checkers.viewModel.ScreenState

@Composable
fun <T : ScreenState> messageCollectionDisposableEffect(viewModel: BaseViewModel<T>) {
    DisposableEffect(Unit) {
        viewModel.startCollecting()

        onDispose {
            viewModel.stopCollecting()
        }
    }
}