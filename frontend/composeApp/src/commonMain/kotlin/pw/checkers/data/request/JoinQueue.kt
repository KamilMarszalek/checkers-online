package pw.checkers.data.request

import kotlinx.serialization.Serializable
import pw.checkers.data.domain.User

@Serializable
data class JoinQueue(
    val user: User
)
