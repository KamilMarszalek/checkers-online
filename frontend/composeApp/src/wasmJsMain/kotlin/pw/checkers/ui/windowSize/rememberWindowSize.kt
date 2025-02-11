package pw.checkers.ui.windowSize

import androidx.compose.runtime.*
import kotlinx.browser.window
import org.w3c.dom.events.Event

/**
 * @return pair containing pixel values of width and height of viewport
 */
@Composable
actual fun rememberWindowSize(): Pair<Int, Int> {
    var size by remember {
        mutableStateOf(
            window.innerWidth to window.innerHeight
        )
    }

    DisposableEffect(Unit) {
        val listener: (Event) -> Unit = {
            size = window.innerWidth to window.innerHeight
        }
        window.addEventListener("resize", listener)

        onDispose {
            window.removeEventListener("resize", listener)
        }
    }

    return size
}