package pw.checkers.navigation

import androidx.core.bundle.Bundle
import androidx.core.uri.UriUtils
import androidx.navigation.NavType
import kotlinx.serialization.json.Json

inline fun <reified T> serializableNavType(isNullableAllowed: Boolean = false) =
    object : NavType<T>(isNullableAllowed) {
        override fun get(bundle: Bundle, key: String): T? {
            return Json.decodeFromString<T>(bundle.getString(key) ?: return null)
        }

        override fun parseValue(value: String): T {
            return Json.decodeFromString<T>(UriUtils.decode(value))
        }

        override fun serializeAsValue(value: T): String {
            return UriUtils.encode(Json.encodeToString(value))
        }

        override fun put(bundle: Bundle, key: String, value: T) {
            bundle.putString(key, Json.encodeToString(value))
        }
    }