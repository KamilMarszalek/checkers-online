package pw.checkers.ui.icons

import androidx.compose.ui.graphics.vector.ImageVector
import pw.checkers.ui.icons.pawns.BlackPawn
import pw.checkers.ui.icons.pawns.BlackQueen
import pw.checkers.ui.icons.pawns.WhitePawn
import pw.checkers.ui.icons.pawns.WhiteQueen
import kotlin.collections.List as ____KtList

public object Pawns

private var __AllIcons: ____KtList<ImageVector>? = null

public val Pawns.AllIcons: ____KtList<ImageVector>
  get() {
    if (__AllIcons != null) {
      return __AllIcons!!
    }
    __AllIcons= listOf(BlackPawn, BlackQueen, WhitePawn, WhiteQueen)
    return __AllIcons!!
  }
