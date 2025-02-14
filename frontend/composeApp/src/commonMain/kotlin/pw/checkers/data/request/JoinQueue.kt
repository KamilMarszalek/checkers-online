package pw.checkers.data.request

import kotlinx.serialization.Serializable
import pw.checkers.data.User

@Serializable
data class JoinQueue(
    val user: User
)
