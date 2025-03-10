package pw.checkers.game.presentation.gameScreen.ui.components.icon

import androidx.compose.ui.graphics.vector.ImageVector
import pw.checkers.game.presentation.gameScreen.ui.components.icon.pawns.BlackPawn
import pw.checkers.game.presentation.gameScreen.ui.components.icon.pawns.BlackQueen
import pw.checkers.game.presentation.gameScreen.ui.components.icon.pawns.WhitePawn
import pw.checkers.game.presentation.gameScreen.ui.components.icon.pawns.WhiteQueen
import kotlin.collections.List as ____KtList

object Pawns

private var __AllIcons: ____KtList<ImageVector>? = null

val Pawns.AllIcons: ____KtList<ImageVector>
  get() {
    if (__AllIcons != null) {
      return __AllIcons!!
    }
    __AllIcons = listOf(BlackPawn, BlackQueen, WhitePawn, WhiteQueen)
    return __AllIcons!!
  }
