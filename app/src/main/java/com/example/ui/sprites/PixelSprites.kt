package com.example.ui.sprites

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.EnemyId
import com.example.ui.theme.ActionOrange
import com.example.ui.theme.BlueSoulCyan
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.KindnessGreen
import com.example.ui.theme.MercyYellow
import com.example.ui.theme.RuinsViolet
import com.example.ui.theme.SoulRed
import kotlin.math.sin

private val HEART_GRID = listOf(
    "..RRR..RRR..",
    ".RRRRRRRRRR.",
    "RRRRRRRRRRRR",
    "RRRRRRRRRRRR",
    "RRRRRRRRRRRR",
    ".RRRRRRRRRR.",
    "..RRRRRRRR..",
    "...RRRRRR...",
    "....RRRR....",
    ".....RR....."
)

private val PAPATYA_GRID = listOf(
    ".....YYYYYY.....",
    "...YYYYYYYYYY...",
    "..YYYWWYYWWYYY..",
    "..YYWKKWWKKWYY..",
    "..YYWKKWWKKWYY..",
    "..YYYWWWWWWYYY..",
    "...YYWKKKKWYY...",
    "....YYYYYYYY....",
    "......GGGG......",
    ".....GGGGGG.....",
    "....GG.GG.GG....",
    ".......GG.......",
    "......GGGG......",
    "....GGGGGGGG...."
)

private val ZIPLAK_GRID = listOf(
    "....WWW....WWW....",
    "...WKKKW..WKKKW...",
    "...WKKKW..WKKKW...",
    "..WWWWWWWWWWWWWW..",
    ".WWWWWWWWWWWWWWWW.",
    ".WWWWWKKKKKKWWWWW.",
    ".WWWWWWWWWWWWWWWW.",
    "..WWWWWWWWWWWWWW..",
    "...WWWWWWWWWWWW...",
    "..WWW.WWWWWW.WWW..",
    ".WWW..WWWWWW..WWW.",
    ".WW....WWWW....WW.",
    "WW..............WW"
)

private val KEMIK_GRID = listOf(
    ".....WWWWWW.....",
    "....WWWWWWWW....",
    "...WWKKWWKKWW...",
    "...WWKKWWKKWW...",
    "...WWWWKKWWWW...",
    "....WWWWWWWW....",
    "....WKWKWKWK....",
    "...RRRRRRRRRR...",
    "..RRRRRRRRRRRR..",
    "...WW.WWWW.WW...",
    "..WWW.WWWW.WWW..",
    "..WW..WWWW..WW..",
    "......W..W......",
    ".....WW..WW.....",
    "....RRR..RRR...."
)

private val VEX_GRID = listOf(
    "..PP..........PP..",
    "..PPP..WWWW..PPP..",
    "...PPPPWWWWPPPP...",
    "....PPPPPPPPPP....",
    "...PPPKKKKKKPPP...",
    "...PPKRRKKRRKPP...",
    "....PPKKKKKKPP....",
    "..PPPPPPPPPPPPPP..",
    ".PPPPPWWWWWWPPPPP.",
    "PPPP.WWWWWWWW.PPPP",
    "PPP..WWPPPPWW..PPP",
    "PP...WWWWWWWW...PP",
    ".....PPP..PPP.....",
    "....PPP....PPP....",
    "...PPPP....PPPP..."
)

private val SAVE_STAR_GRID = listOf(
    "....YY....",
    "....YY....",
    "..YYYYYY..",
    "YYYYWWYYYY",
    "YYYYWWYYYY",
    "..YYYYYY..",
    "....YY....",
    "....YY...."
)

@Composable
fun PixelHeartIcon(
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    color: Color = SoulRed
) {
    Canvas(modifier = modifier.size(size)) {
        drawPixelGrid(
            grid = HEART_GRID,
            palette = mapOf('R' to color)
        )
    }
}

@Composable
fun SaveStarIcon(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    val transition = rememberInfiniteTransition(label = "save_star")
    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    Canvas(modifier = modifier.size(size)) {
        drawPixelGrid(
            grid = SAVE_STAR_GRID,
            palette = mapOf(
                'Y' to MercyYellow.copy(alpha = (pulse - 0.1f).coerceIn(0.6f, 1f)),
                'W' to CrispWhite
            )
        )
    }
}

@Composable
fun EnemyPixelSprite(
    enemyId: EnemyId,
    isSpareable: Boolean,
    isHurt: Boolean,
    shakeOffsetX: Float,
    modifier: Modifier = Modifier,
    size: Dp = 128.dp
) {
    val transition = rememberInfiniteTransition(label = "enemy_idle")
    val bobPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "bob"
    )

    val grid = when (enemyId) {
        EnemyId.PAPATYA -> PAPATYA_GRID
        EnemyId.ZIPLAK -> ZIPLAK_GRID
        EnemyId.KEMIK_SOVALYE -> KEMIK_GRID
        EnemyId.GOLGE_VEX -> VEX_GRID
    }

    val tintAlpha = if (isSpareable) 0.72f else 1f
    val bobY = sin(bobPhase.toDouble()).toFloat() * 6f

    Canvas(modifier = modifier.size(size)) {
        val palette = when (enemyId) {
            EnemyId.PAPATYA -> mapOf(
                'Y' to (if (isHurt) SoulRed else MercyYellow).copy(alpha = tintAlpha),
                'W' to (if (isSpareable) Color(0xFFFFD6E0) else CrispWhite),
                'K' to Color(0xFF0A0A0F),
                'G' to KindnessGreen.copy(alpha = tintAlpha)
            )
            EnemyId.ZIPLAK -> mapOf(
                'W' to (if (isHurt) SoulRed else if (isSpareable) Color(0xFFD9FFD9) else CrispWhite).copy(alpha = tintAlpha),
                'K' to Color(0xFF0A0A0F)
            )
            EnemyId.KEMIK_SOVALYE -> mapOf(
                'W' to (if (isHurt) SoulRed else CrispWhite).copy(alpha = tintAlpha),
                'K' to Color(0xFF0A0A0F),
                'R' to (if (isSpareable) MercyYellow else ActionOrange).copy(alpha = tintAlpha)
            )
            EnemyId.GOLGE_VEX -> mapOf(
                'P' to (if (isHurt) SoulRed else if (isSpareable) BlueSoulCyan else RuinsViolet).copy(alpha = tintAlpha),
                'W' to CrispWhite.copy(alpha = tintAlpha),
                'K' to Color(0xFF0A0A0F),
                'R' to (if (isSpareable) KindnessGreen else SoulRed)
            )
        }

        drawPixelGrid(
            grid = grid,
            palette = palette,
            offset = Offset(shakeOffsetX, bobY)
        )
    }
}

fun DrawScope.drawPixelGrid(
    grid: List<String>,
    palette: Map<Char, Color>,
    offset: Offset = Offset.Zero,
    areaSize: Size = this.size
) {
    if (grid.isEmpty()) return
    val rows = grid.size
    val cols = grid.maxOf { it.length }
    val cellW = areaSize.width / cols
    val cellH = areaSize.height / rows
    val cell = minOf(cellW, cellH)
    val startX = offset.x + (areaSize.width - cell * cols) / 2f
    val startY = offset.y + (areaSize.height - cell * rows) / 2f

    for (r in 0 until rows) {
        val row = grid[r]
        for (c in row.indices) {
            val ch = row[c]
            val color = palette[ch] ?: continue
            drawRect(
                color = color,
                topLeft = Offset(startX + c * cell, startY + r * cell),
                size = Size(cell + 0.6f, cell + 0.6f)
            )
        }
    }
}

fun DrawScope.drawHeartInBox(
    centerX: Float,
    centerY: Float,
    radiusPx: Float,
    color: Color
) {
    val boxSize = radiusPx * 2.2f
    drawPixelGrid(
        grid = HEART_GRID,
        palette = mapOf('R' to color),
        offset = Offset(centerX - boxSize / 2f, centerY - boxSize / 2f),
        areaSize = Size(boxSize, boxSize)
    )
}
