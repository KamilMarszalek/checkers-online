package pw.checkers.game.presentation.gameScreen.ui.components.icon.pawns

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import pw.checkers.game.presentation.gameScreen.ui.components.icon.Pawns

val Pawns.BlackPawn: ImageVector
    get() {
        if (_blackpawn != null) {
            return _blackpawn!!
        }
        _blackpawn = Builder(name = "Blackpawn", defaultWidth = 257.0.dp, defaultHeight = 256.0.dp,
                viewportWidth = 257.0f, viewportHeight = 256.0f).apply {
            path(fill = SolidColor(Color(0xFF111111)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(128.4f, 220.12f)
                curveTo(156.38f, 220.12f, 181.99f, 212.39f, 200.77f, 199.58f)
                curveTo(219.51f, 186.8f, 231.96f, 168.51f, 231.96f, 147.57f)
                verticalLineTo(109.54f)
                curveTo(231.96f, 88.6f, 219.51f, 70.32f, 200.77f, 57.53f)
                curveTo(181.99f, 44.73f, 156.38f, 37.0f, 128.4f, 37.0f)
                curveTo(100.41f, 37.0f, 74.8f, 44.73f, 56.03f, 57.53f)
                curveTo(37.28f, 70.32f, 24.83f, 88.6f, 24.83f, 109.54f)
                verticalLineTo(147.57f)
                curveTo(24.83f, 168.51f, 37.28f, 186.8f, 56.03f, 199.58f)
                curveTo(74.8f, 212.39f, 100.41f, 220.12f, 128.4f, 220.12f)
                close()
            }
            path(fill = SolidColor(Color(0xFF666666)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(225.96f, 147.57f)
                curveTo(225.96f, 184.32f, 182.28f, 214.11f, 128.4f, 214.11f)
                curveTo(74.51f, 214.11f, 30.83f, 184.32f, 30.83f, 147.57f)
                verticalLineTo(109.54f)
                curveTo(30.83f, 72.79f, 74.51f, 43.0f, 128.4f, 43.0f)
                curveTo(182.28f, 43.0f, 225.96f, 72.79f, 225.96f, 109.54f)
                verticalLineTo(147.57f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, fillAlpha = 0.5f,
                    strokeLineWidth = 0.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(128.4f, 214.11f)
                curveTo(182.28f, 214.11f, 225.96f, 184.32f, 225.96f, 147.57f)
                verticalLineTo(109.55f)
                curveTo(225.96f, 146.3f, 182.28f, 176.09f, 128.4f, 176.09f)
                curveTo(74.51f, 176.09f, 30.83f, 146.3f, 30.83f, 109.55f)
                verticalLineTo(147.57f)
                curveTo(30.83f, 184.32f, 74.51f, 214.11f, 128.4f, 214.11f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, fillAlpha = 0.5f,
                    strokeLineWidth = 0.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(208.28f, 109.19f)
                curveTo(207.95f, 86.56f, 172.74f, 68.27f, 129.35f, 68.27f)
                curveTo(85.96f, 68.27f, 50.75f, 86.56f, 50.42f, 109.19f)
                curveTo(50.42f, 109.06f, 50.42f, 108.93f, 50.42f, 108.8f)
                curveTo(50.42f, 80.92f, 85.76f, 58.31f, 129.35f, 58.31f)
                curveTo(172.94f, 58.31f, 208.28f, 80.92f, 208.28f, 108.8f)
                curveTo(208.28f, 108.93f, 208.28f, 109.06f, 208.28f, 109.19f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, fillAlpha = 0.2f,
                    strokeLineWidth = 0.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(129.35f, 159.29f)
                curveTo(170.9f, 159.29f, 204.95f, 138.75f, 208.05f, 112.68f)
                lineTo(208.28f, 109.19f)
                curveTo(207.95f, 86.56f, 172.74f, 68.27f, 129.35f, 68.27f)
                curveTo(85.96f, 68.27f, 50.75f, 86.56f, 50.42f, 109.19f)
                lineTo(50.65f, 112.68f)
                curveTo(53.75f, 138.75f, 87.8f, 159.29f, 129.35f, 159.29f)
                close()
            }
        }
        .build()
        return _blackpawn!!
    }

private var _blackpawn: ImageVector? = null
