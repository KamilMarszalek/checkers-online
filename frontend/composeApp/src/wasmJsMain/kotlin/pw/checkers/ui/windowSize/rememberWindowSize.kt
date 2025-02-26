package pw.checkers.ui.windowSize

import androidx.compose.runtime.*
import kotlinx.browser.window
import org.w3c.dom.events.Event

/**
 * @return pair containing pixel values of width and height of viewport
 */
@Composable
actual fun rememberWindowSize(): WindowSize {
    var size by remember {
        mutableStateOf(
            WindowSize(window.innerWidth, window.innerHeight)
        )
    }

    DisposableEffect(Unit) {
        val listener: (Event) -> Unit = {
            val newWidth = window.innerWidth
            val newHeight = window.innerHeight
            if (newWidth != size.width || newHeight != size.height) {
                size = WindowSize(newWidth, newHeight)
            }
        }
        window.addEventListener("resize", listener)

        onDispose {
            window.removeEventListener("resize", listener)
        }
    }

    return size
}