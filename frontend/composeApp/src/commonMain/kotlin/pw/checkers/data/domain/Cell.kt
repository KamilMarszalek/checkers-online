package pw.checkers.data.domain

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class Cell (

    @JsonNames("capturedRow")
    val row: Int,

    @JsonNames("capturedCol")
    val col: Int,

    val piece: Piece? = null,
)
