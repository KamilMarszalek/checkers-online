package pw.checkers.game.util

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min

fun calcCellSize(width: Int, height: Int, minCellSize: Dp = 20.dp): Dp {
    val cellFromHeight = (height / 10).dp
    val cellFromWidth = (width / 8).dp
    return max(minCellSize, min(cellFromWidth, cellFromHeight))
}