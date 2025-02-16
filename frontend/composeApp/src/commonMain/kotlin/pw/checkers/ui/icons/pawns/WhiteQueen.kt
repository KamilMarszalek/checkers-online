package pw.checkers.ui.icons.pawns

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import kotlin.Unit
import pw.checkers.ui.icons.Pawns

public val Pawns.WhiteQueen: ImageVector
    get() {
        if (_whitequeen != null) {
            return _whitequeen!!
        }
        _whitequeen = Builder(name = "Whitequeen", defaultWidth = 257.0.dp, defaultHeight =
                256.0.dp, viewportWidth = 257.0f, viewportHeight = 256.0f).apply {
            path(fill = SolidColor(Color(0xFF444444)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(128.67f, 220.12f)
                curveTo(156.66f, 220.12f, 182.27f, 212.39f, 201.04f, 199.58f)
                curveTo(219.79f, 186.8f, 232.24f, 168.51f, 232.24f, 147.57f)
                verticalLineTo(109.54f)
                curveTo(232.24f, 88.6f, 219.79f, 70.32f, 201.04f, 57.53f)
                curveTo(182.27f, 44.73f, 156.66f, 37.0f, 128.67f, 37.0f)
                curveTo(100.68f, 37.0f, 75.08f, 44.73f, 56.3f, 57.53f)
                curveTo(37.56f, 70.32f, 25.11f, 88.6f, 25.11f, 109.54f)
                verticalLineTo(147.57f)
                curveTo(25.11f, 168.51f, 37.56f, 186.8f, 56.3f, 199.58f)
                curveTo(75.08f, 212.39f, 100.68f, 220.12f, 128.67f, 220.12f)
                close()
            }
            path(fill = SolidColor(Color(0xFFEEEEEE)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(226.23f, 147.57f)
                curveTo(226.23f, 184.32f, 182.55f, 214.11f, 128.67f, 214.11f)
                curveTo(74.79f, 214.11f, 31.11f, 184.32f, 31.11f, 147.57f)
                verticalLineTo(109.54f)
                curveTo(31.11f, 72.79f, 74.79f, 43.0f, 128.67f, 43.0f)
                curveTo(182.55f, 43.0f, 226.23f, 72.79f, 226.23f, 109.54f)
                verticalLineTo(147.57f)
                close()
            }
            path(fill = SolidColor(Color(0xFFff0000)), stroke = null, fillAlpha = 0.6f,
                    strokeLineWidth = 0.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(128.67f, 214.11f)
                curveTo(182.55f, 214.11f, 226.23f, 184.32f, 226.23f, 147.57f)
                verticalLineTo(109.55f)
                curveTo(226.23f, 146.3f, 182.55f, 176.09f, 128.67f, 176.09f)
                curveTo(74.79f, 176.09f, 31.11f, 146.3f, 31.11f, 109.55f)
                verticalLineTo(147.57f)
                curveTo(31.11f, 184.32f, 74.79f, 214.11f, 128.67f, 214.11f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, fillAlpha = 0.4f,
                    strokeLineWidth = 0.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(208.56f, 109.19f)
                curveTo(208.23f, 86.56f, 173.01f, 68.27f, 129.63f, 68.27f)
                curveTo(86.24f, 68.27f, 51.02f, 86.56f, 50.69f, 109.19f)
                curveTo(50.69f, 109.06f, 50.69f, 108.93f, 50.69f, 108.8f)
                curveTo(50.69f, 80.92f, 86.03f, 58.31f, 129.63f, 58.31f)
                curveTo(173.22f, 58.31f, 208.56f, 80.92f, 208.56f, 108.8f)
                curveTo(208.56f, 108.93f, 208.56f, 109.06f, 208.56f, 109.19f)
                close()
            }
            path(fill = SolidColor(Color(0xFF000000)), stroke = null, fillAlpha = 0.15f,
                    strokeLineWidth = 0.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(129.63f, 159.29f)
                curveTo(171.18f, 159.29f, 205.23f, 138.75f, 208.33f, 112.68f)
                lineTo(208.56f, 109.19f)
                curveTo(208.23f, 86.56f, 173.01f, 68.27f, 129.63f, 68.27f)
                curveTo(86.24f, 68.27f, 51.02f, 86.56f, 50.69f, 109.19f)
                lineTo(50.92f, 112.68f)
                curveTo(54.02f, 138.75f, 88.07f, 159.29f, 129.63f, 159.29f)
                close()
            }
        }
        .build()
        return _whitequeen!!
    }

private var _whitequeen: ImageVector? = null
