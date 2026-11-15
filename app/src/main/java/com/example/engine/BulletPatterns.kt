package com.example.engine

import com.example.model.Bullet
import com.example.model.BulletShape
import com.example.model.BulletType
import com.example.model.EnemyId
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object BulletPatterns {
    private var nextId = 1L
    private fun id() = nextId++

    fun spawnWave(
        enemyId: EnemyId,
        turnNumber: Int,
        waveIndex: Int,
        soulX: Float,
        soulY: Float
    ): List<Bullet> {
        return when (enemyId) {
            EnemyId.PAPATYA -> spawnPapatyaWave(turnNumber, waveIndex, soulX, soulY)
            EnemyId.ZIPLAK -> spawnZiplakWave(turnNumber, waveIndex)
            EnemyId.KEMIK_SOVALYE -> spawnKemikWave(turnNumber, waveIndex)
            EnemyId.GOLGE_VEX -> spawnVexWave(turnNumber, waveIndex, soulX, soulY)
        }
    }

    private fun spawnPapatyaWave(
        turn: Int,
        wave: Int,
        soulX: Float,
        soulY: Float
    ): List<Bullet> {
        val list = mutableListOf<Bullet>()
        if (wave % 2 == 0) {
            // Aimed "friendliness" pellets from top corners
            val startX = if ((wave / 2) % 2 == 0) 0.15f else 0.85f
            val startY = 0.08f
            val dx = soulX - startX
            val dy = soulY - startY
            val len = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat().coerceAtLeast(0.01f)
            val speed = 0.42f + (turn * 0.03f).coerceAtMost(0.18f)
            for (spread in -1..1) {
                val angle = kotlin.math.atan2(dy.toDouble(), dx.toDouble()) + spread * 0.22
                list += Bullet(
                    id = id(),
                    x = startX,
                    y = startY,
                    vx = (cos(angle) * speed).toFloat(),
                    vy = (sin(angle) * speed).toFloat(),
                    radius = 0.032f,
                    type = if (wave == 4 && spread == 0) BulletType.GREEN_HEAL else BulletType.WHITE,
                    shape = BulletShape.STAR,
                    spinDegPerSec = 180f
                )
            }
        } else {
            // Ring rain from top
            val count = 4 + (turn % 2)
            for (i in 0 until count) {
                val xPos = 0.12f + i * (0.76f / (count - 1).coerceAtLeast(1)) + Random.nextFloat() * 0.05f
                list += Bullet(
                    id = id(),
                    x = xPos.coerceIn(0.08f, 0.92f),
                    y = 0.02f,
                    vx = (Random.nextFloat() - 0.5f) * 0.12f,
                    vy = 0.36f + Random.nextFloat() * 0.14f,
                    radius = 0.028f,
                    type = BulletType.WHITE,
                    shape = BulletShape.PELLET
                )
            }
        }
        return list
    }

    private fun spawnZiplakWave(turn: Int, wave: Int): List<Bullet> {
        val list = mutableListOf<Bullet>()
        if (wave % 3 == 0) {
            // Sideways hopping frog/fly sweep
            val fromLeft = (wave % 2 == 0)
            val xStart = if (fromLeft) 0.02f else 0.98f
            val vx = if (fromLeft) 0.44f else -0.44f
            for (k in 0..2) {
                list += Bullet(
                    id = id(),
                    x = xStart,
                    y = 0.22f + k * 0.28f + (Random.nextFloat() - 0.5f) * 0.08f,
                    vx = vx,
                    vy = (Random.nextFloat() - 0.5f) * 0.12f,
                    radius = 0.034f,
                    type = if (k == 1 && wave == 6) BulletType.GREEN_HEAL else BulletType.WHITE,
                    shape = BulletShape.PELLET
                )
            }
        } else {
            // Rising bubbles from bottom
            val count = 3 + (turn % 2)
            for (i in 0 until count) {
                list += Bullet(
                    id = id(),
                    x = 0.12f + Random.nextFloat() * 0.76f,
                    y = 0.98f,
                    vx = (Random.nextFloat() - 0.5f) * 0.18f,
                    vy = -(0.34f + Random.nextFloat() * 0.18f),
                    radius = 0.030f,
                    type = BulletType.WHITE,
                    shape = BulletShape.PELLET
                )
            }
        }
        return list
    }

    private fun spawnKemikWave(turn: Int, wave: Int): List<Bullet> {
        val list = mutableListOf<Bullet>()
        when (wave % 4) {
            0 -> {
                // Tall BLUE bone sweeping across (stand still to avoid!)
                val fromLeft = (turn + wave) % 2 == 0
                list += Bullet(
                    id = id(),
                    x = if (fromLeft) 0.02f else 0.98f,
                    y = 0.5f,
                    vx = if (fromLeft) 0.42f else -0.42f,
                    vy = 0f,
                    halfWidth = 0.026f,
                    halfHeight = 0.46f,
                    type = BulletType.CYAN_STILL,
                    shape = BulletShape.BONE_V
                )
            }
            2 -> {
                // Tall ORANGE bone sweeping across (keep moving to pass safely!)
                val fromLeft = (turn + wave) % 2 != 0
                list += Bullet(
                    id = id(),
                    x = if (fromLeft) 0.02f else 0.98f,
                    y = 0.5f,
                    vx = if (fromLeft) 0.45f else -0.45f,
                    vy = 0f,
                    halfWidth = 0.026f,
                    halfHeight = 0.46f,
                    type = BulletType.ORANGE_MOVE,
                    shape = BulletShape.BONE_V
                )
            }
            else -> {
                // Gap bone corridor: top & bottom bone pair with a safe gap!
                val fromLeft = wave % 2 == 1
                val vx = if (fromLeft) 0.40f else -0.40f
                val x = if (fromLeft) 0.03f else 0.97f
                val gapCenter = 0.30f + Random.nextFloat() * 0.40f
                val gapHalf = 0.16f
                val topEnd = (gapCenter - gapHalf).coerceAtLeast(0.08f)
                val bottomStart = (gapCenter + gapHalf).coerceAtMost(0.92f)

                list += Bullet(
                    id = id(),
                    x = x,
                    y = topEnd / 2f,
                    vx = vx,
                    vy = 0f,
                    halfWidth = 0.024f,
                    halfHeight = topEnd / 2f,
                    type = BulletType.WHITE,
                    shape = BulletShape.BONE_V
                )
                list += Bullet(
                    id = id(),
                    x = x,
                    y = (1f + bottomStart) / 2f,
                    vx = vx,
                    vy = 0f,
                    halfWidth = 0.024f,
                    halfHeight = (1f - bottomStart) / 2f,
                    type = BulletType.WHITE,
                    shape = BulletShape.BONE_V
                )
                if (wave == 5) {
                    list += Bullet(
                        id = id(),
                        x = x,
                        y = gapCenter,
                        vx = vx,
                        vy = 0f,
                        radius = 0.03f,
                        type = BulletType.GREEN_HEAL,
                        shape = BulletShape.PELLET
                    )
                }
            }
        }
        return list
    }

    private fun spawnVexWave(
        turn: Int,
        wave: Int,
        soulX: Float,
        soulY: Float
    ): List<Bullet> {
        val list = mutableListOf<Bullet>()
        when (wave % 4) {
            0 -> {
                // Starburst ring from center-top
                val originX = 0.5f
                val originY = 0.12f
                val count = 7
                val offsetAngle = (wave * 0.35f)
                for (i in 0 until count) {
                    val a = offsetAngle + i * (PI.toFloat() / (count - 1))
                    list += Bullet(
                        id = id(),
                        x = originX,
                        y = originY,
                        vx = cos(a) * 0.40f,
                        vy = sin(a) * 0.40f,
                        radius = 0.028f,
                        type = if (i == 3 && wave % 8 == 0) BulletType.GREEN_HEAL else BulletType.WHITE,
                        shape = BulletShape.STAR,
                        spinDegPerSec = 240f
                    )
                }
            }
            1 -> {
                // Aimed spears from sides
                val fromLeft = turn % 2 == 0
                val sx = if (fromLeft) 0.04f else 0.96f
                val sy = 0.18f + Random.nextFloat() * 0.64f
                val dx = soulX - sx
                val dy = soulY - sy
                val len = kotlin.math.hypot(dx.toDouble(), dy.toDouble()).toFloat().coerceAtLeast(0.01f)
                val speed = 0.56f
                list += Bullet(
                    id = id(),
                    x = sx,
                    y = sy,
                    vx = (dx / len) * speed,
                    vy = (dy / len) * speed,
                    halfWidth = 0.045f,
                    halfHeight = 0.016f,
                    type = BulletType.WHITE,
                    shape = BulletShape.SPEAR,
                    rotationDeg = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                )
            }
            2 -> {
                // Alternating Cyan (stand still) or Orange (move) horizontal wave!
                val isCyan = (wave / 2 + turn) % 2 == 0
                list += Bullet(
                    id = id(),
                    x = 0.5f,
                    y = 0.03f,
                    vx = 0f,
                    vy = 0.44f,
                    halfWidth = 0.46f,
                    halfHeight = 0.022f,
                    type = if (isCyan) BulletType.CYAN_STILL else BulletType.ORANGE_MOVE,
                    shape = BulletShape.BONE_H
                )
            }
            else -> {
                // Diagonal cross spears
                list += Bullet(
                    id = id(),
                    x = 0.08f,
                    y = 0.05f,
                    vx = 0.36f,
                    vy = 0.42f,
                    radius = 0.028f,
                    type = BulletType.WHITE,
                    shape = BulletShape.STAR,
                    spinDegPerSec = 200f
                )
                list += Bullet(
                    id = id(),
                    x = 0.92f,
                    y = 0.05f,
                    vx = -0.36f,
                    vy = 0.42f,
                    radius = 0.028f,
                    type = BulletType.WHITE,
                    shape = BulletShape.STAR,
                    spinDegPerSec = -200f
                )
            }
        }
        return list
    }
}
