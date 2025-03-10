package pw.checkers.core.presentation

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

class UiText(
    private val resource: StringResource,
    private val args: Array<Any> = emptyArray()
) {
    @Composable
    fun asString() = stringResource(resource, formatArgs = args)
}