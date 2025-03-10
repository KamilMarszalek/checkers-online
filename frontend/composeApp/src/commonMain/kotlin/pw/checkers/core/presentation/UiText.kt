package pw.checkers.core.presentation

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

sealed interface  UiText {
    data class DynamicString(val value: String) : UiText

    class StringResourceId(
        val resource: StringResource,
        val args: Array<Any> = emptyArray()
    ) : UiText

    @Composable
    fun asString(): String {
        return when(this) {
            is DynamicString -> value
            is StringResourceId -> stringResource(resource, formatArgs = args)
        }
    }
}