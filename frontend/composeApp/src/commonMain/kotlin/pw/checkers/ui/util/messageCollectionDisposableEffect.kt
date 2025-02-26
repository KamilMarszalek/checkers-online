package pw.checkers.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import pw.checkers.viewModel.BaseViewModel

@Composable
fun messageCollectionDisposableEffect(viewModel: BaseViewModel) {
    DisposableEffect(Unit) {
        viewModel.startCollecting()

        onDispose {
            viewModel.stopCollecting()
        }
    }
}