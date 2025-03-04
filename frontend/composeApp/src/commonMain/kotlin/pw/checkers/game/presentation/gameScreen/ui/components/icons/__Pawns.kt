package pw.checkers.game.presentation.gameScreen.ui.components.icons

import androidx.compose.ui.graphics.vector.ImageVector
import pw.checkers.game.presentation.gameScreen.ui.components.icons.pawns.BlackPawn
import pw.checkers.game.presentation.gameScreen.ui.components.icons.pawns.BlackQueen
import pw.checkers.game.presentation.gameScreen.ui.components.icons.pawns.WhitePawn
import pw.checkers.game.presentation.gameScreen.ui.components.icons.pawns.WhiteQueen
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
