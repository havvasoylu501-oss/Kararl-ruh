package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.GameContent
import com.example.model.Direction
import com.example.model.EnemyId
import com.example.model.GameUiState
import com.example.model.OverworldRoom
import com.example.model.TileType
import com.example.ui.sprites.PixelHeartIcon
import com.example.ui.theme.ActionOrange
import com.example.ui.theme.BlueSoulCyan
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.HpRed
import com.example.ui.theme.KindnessGreen
import com.example.ui.theme.MercyYellow
import com.example.ui.theme.RuinsBorder
import com.example.ui.theme.RuinsSurface
import com.example.ui.theme.RuinsViolet
import com.example.ui.theme.SoftGray
import com.example.ui.theme.SoulRed
import com.example.ui.theme.VoidBlack

@Composable
fun OverworldScreen(
    uiState: GameUiState,
    onMoveDirection: (Direction?) -> Unit,
    onInteract: () -> Unit,
    onAdvanceDialog: () -> Unit,
    onToggleMute: () -> Unit,
    onBackToTitle: () -> Unit
) {
    BackHandler {
        onBackToTitle()
    }

    val roomDef = GameContent.rooms[uiState.currentRoom]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top RPG HUD
            Surface(
                color = DeepObsidian,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(2.dp, CrispWhite)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackToTitle,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("overworld_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Ana menüye dön",
                            tint = CrispWhite
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.currentRoom.title.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MercyYellow
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${uiState.playerName}  LV ${uiState.lv}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CrispWhite
                            )
                            Text(
                                text = "HP ${uiState.hp}/${uiState.maxHp}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MercyYellow
                            )
                            Text(
                                text = "${uiState.gold}G",
                                style = MaterialTheme.typography.labelSmall,
                                color = ActionOrange
                            )
                        }
                    }
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("overworld_mute_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isMuted) {
                                Icons.AutoMirrored.Filled.VolumeOff
                            } else {
                                Icons.AutoMirrored.Filled.VolumeUp
                            },
                            contentDescription = "Ses aç/kapat",
                            tint = CrispWhite
                        )
                    }
                }
            }

            // Tilemap Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(12f / 10f)
                    .background(VoidBlack, RoundedCornerShape(4.dp))
                    .border(2.dp, CrispWhite, RoundedCornerShape(4.dp))
            ) {
                if (roomDef != null) {
                    OverworldMapCanvas(
                        room = uiState.currentRoom,
                        grid = roomDef.grid,
                        playerX = uiState.playerX,
                        playerY = uiState.playerY,
                        direction = uiState.playerDirection,
                        switchPressed = uiState.switchPressed,
                        sparedEnemies = uiState.sparedEnemies,
                        killedEnemies = uiState.killedEnemies
                    )
                }

                uiState.statusToast?.let { toast ->
                    Surface(
                        color = VoidBlack.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, MercyYellow),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = toast,
                            style = MaterialTheme.typography.labelSmall,
                            color = MercyYellow,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Dialogue Box OR Virtual Controls
            val dialog = uiState.overworldDialog
            if (dialog != null) {
                val fullText = dialog.lines.getOrNull(dialog.currentLineIndex) ?: ""
                val shownText = fullText.take(uiState.visibleDialogChars.coerceAtMost(fullText.length))
                Surface(
                    color = VoidBlack,
                    border = BorderStroke(3.dp, CrispWhite),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable { onAdvanceDialog() }
                        .testTag("overworld_dialog_box")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = dialog.speaker,
                                style = MaterialTheme.typography.labelLarge,
                                color = MercyYellow
                            )
                            Text(
                                text = shownText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = CrispWhite
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PixelHeartIcon(size = 14.dp, color = SoulRed)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DEVAM ETMEK İÇİN DOKUN ▶",
                                style = MaterialTheme.typography.labelSmall,
                                color = ActionOrange
                            )
                        }
                    }
                }
            } else {
                // Ergonomic D-Pad + Interact Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // D-Pad
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DPadButton(
                            icon = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Yukarı yürü",
                            testTag = "dpad_up",
                            onHoldChanged = { held ->
                                onMoveDirection(if (held) Direction.UP else null)
                            }
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DPadButton(
                                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Sola yürü",
                                testTag = "dpad_left",
                                onHoldChanged = { held ->
                                    onMoveDirection(if (held) Direction.LEFT else null)
                                }
                            )
                            DPadButton(
                                icon = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Aşağı yürü",
                                testTag = "dpad_down",
                                onHoldChanged = { held ->
                                    onMoveDirection(if (held) Direction.DOWN else null)
                                }
                            )
                            DPadButton(
                                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Sağa yürü",
                                testTag = "dpad_right",
                                onHoldChanged = { held ->
                                    onMoveDirection(if (held) Direction.RIGHT else null)
                                }
                            )
                        }
                    }

                    // Interact Button & Hint
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Yürü ve İNCELE'ye bas",
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftGray
                        )
                        Button(
                            onClick = onInteract,
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DeepObsidian,
                                contentColor = MercyYellow
                            ),
                            border = BorderStroke(2.dp, MercyYellow),
                            modifier = Modifier
                                .height(64.dp)
                                .widthIn(min = 144.dp)
                                .testTag("overworld_interact_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MercyYellow
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "İNCELE (Z)",
                                style = MaterialTheme.typography.titleLarge,
                                color = MercyYellow
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DPadButton(
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    onHoldChanged: (Boolean) -> Unit
) {
    Surface(
        color = RuinsSurface,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(2.dp, CrispWhite.copy(alpha = 0.85f)),
        modifier = Modifier
            .size(56.dp)
            .testTag(testTag)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    onHoldChanged(true)
                    waitForUpOrCancellation()
                    onHoldChanged(false)
                }
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = CrispWhite,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun OverworldMapCanvas(
    room: OverworldRoom,
    grid: List<String>,
    playerX: Float,
    playerY: Float,
    direction: Direction,
    switchPressed: Boolean,
    sparedEnemies: Set<EnemyId>,
    killedEnemies: Set<EnemyId>
) {
    val wallColor = when (room) {
        OverworldRoom.RUINS_ENTRANCE, OverworldRoom.MOSSY_CORRIDOR -> Color(0xFF4A2559)
        OverworldRoom.SNOWY_OUTPOST -> Color(0xFF284866)
        OverworldRoom.THRONE_HALL -> Color(0xFF3E291A)
    }
    val floorColor = when (room) {
        OverworldRoom.RUINS_ENTRANCE, OverworldRoom.MOSSY_CORRIDOR -> Color(0xFF1A1123)
        OverworldRoom.SNOWY_OUTPOST -> Color(0xFF13202E)
        OverworldRoom.THRONE_HALL -> Color(0xFF17120E)
    }
    val pathColor = when (room) {
        OverworldRoom.RUINS_ENTRANCE, OverworldRoom.MOSSY_CORRIDOR -> Color(0xFF2B1D38)
        OverworldRoom.SNOWY_OUTPOST -> Color(0xFF1F3448)
        OverworldRoom.THRONE_HALL -> Color(0xFF38291A)
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val rows = grid.size
        val cols = 12
        val tileW = size.width / cols
        val tileH = size.height / rows

        for (r in 0 until rows) {
            val rowStr = grid[r]
            for (c in 0 until cols) {
                val ch = rowStr.getOrNull(c) ?: '#'
                val tile = GameContent.parseTile(ch, switchPressed)
                val left = c * tileW
                val top = r * tileH
                val tSize = Size(tileW + 0.8f, tileH + 0.8f)

                when (tile) {
                    TileType.WALL -> {
                        drawRect(color = wallColor, topLeft = Offset(left, top), size = tSize)
                        drawRect(
                            color = Color.Black.copy(alpha = 0.25f),
                            topLeft = Offset(left + 2f, top + tileH * 0.72f),
                            size = Size(tileW - 4f, tileH * 0.25f)
                        )
                    }
                    TileType.PATH -> {
                        drawRect(color = pathColor, topLeft = Offset(left, top), size = tSize)
                    }
                    TileType.PORTAL_NEXT, TileType.PORTAL_PREV -> {
                        drawRect(color = pathColor, topLeft = Offset(left, top), size = tSize)
                        drawRect(
                            color = MercyYellow.copy(alpha = 0.35f),
                            topLeft = Offset(left + tileW * 0.15f, top + tileH * 0.15f),
                            size = Size(tileW * 0.7f, tileH * 0.7f)
                        )
                    }
                    else -> {
                        drawRect(color = floorColor, topLeft = Offset(left, top), size = tSize)
                    }
                }

                val cx = left + tileW / 2f
                val cy = top + tileH / 2f

                when (tile) {
                    TileType.SAVE_STAR -> {
                        drawCircle(
                            color = MercyYellow.copy(alpha = 0.22f),
                            radius = tileW * 0.42f,
                            center = Offset(cx, cy)
                        )
                        drawRect(
                            color = MercyYellow,
                            topLeft = Offset(cx - tileW * 0.22f, cy - tileH * 0.08f),
                            size = Size(tileW * 0.44f, tileH * 0.16f)
                        )
                        drawRect(
                            color = MercyYellow,
                            topLeft = Offset(cx - tileW * 0.08f, cy - tileH * 0.22f),
                            size = Size(tileW * 0.16f, tileH * 0.44f)
                        )
                        drawCircle(color = CrispWhite, radius = tileW * 0.08f, center = Offset(cx, cy))
                    }
                    TileType.SIGN -> {
                        drawRect(
                            color = Color(0xFF9C6B3B),
                            topLeft = Offset(cx - tileW * 0.28f, cy - tileH * 0.26f),
                            size = Size(tileW * 0.56f, tileH * 0.42f)
                        )
                        drawRect(
                            color = Color(0xFF5D3A1A),
                            topLeft = Offset(cx - tileW * 0.07f, cy + tileH * 0.16f),
                            size = Size(tileW * 0.14f, tileH * 0.22f)
                        )
                    }
                    TileType.CANDY_BOWL -> {
                        drawCircle(
                            color = CrispWhite,
                            radius = tileW * 0.28f,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = RuinsViolet,
                            radius = tileW * 0.15f,
                            center = Offset(cx, cy - tileH * 0.04f)
                        )
                    }
                    TileType.SWITCH_PLATE -> {
                        drawRect(
                            color = if (switchPressed) KindnessGreen else MercyYellow,
                            topLeft = Offset(left + tileW * 0.2f, top + tileH * 0.2f),
                            size = Size(tileW * 0.6f, tileH * 0.6f)
                        )
                    }
                    TileType.SPIKES_CLOSED -> {
                        drawRect(
                            color = HpRed,
                            topLeft = Offset(left + 4f, top + 4f),
                            size = Size(tileW - 8f, tileH - 8f)
                        )
                        drawCircle(color = CrispWhite, radius = tileW * 0.14f, center = Offset(cx, cy))
                    }
                    TileType.SPIKES_OPEN -> {
                        drawRect(
                            color = RuinsBorder,
                            topLeft = Offset(left + 6f, top + 6f),
                            size = Size(tileW - 12f, tileH - 12f)
                        )
                    }
                    TileType.NPC_PAPATYA -> {
                        drawNpcMarker(cx, cy, tileW, MercyYellow, EnemyId.PAPATYA, sparedEnemies, killedEnemies)
                    }
                    TileType.NPC_ZIPLAK -> {
                        drawNpcMarker(cx, cy, tileW, CrispWhite, EnemyId.ZIPLAK, sparedEnemies, killedEnemies)
                    }
                    TileType.NPC_KEMIK -> {
                        drawNpcMarker(cx, cy, tileW, ActionOrange, EnemyId.KEMIK_SOVALYE, sparedEnemies, killedEnemies)
                    }
                    TileType.NPC_VEX -> {
                        drawNpcMarker(cx, cy, tileW, RuinsViolet, EnemyId.GOLGE_VEX, sparedEnemies, killedEnemies)
                    }
                    else -> Unit
                }
            }
        }

        // Draw Player Character (Frisk-inspired striped sweater adventurer)
        val px = playerX * tileW
        val py = playerY * tileH
        val bodyW = tileW * 0.56f
        val bodyH = tileH * 0.68f
        // Shadow
        drawCircle(
            color = Color.Black.copy(alpha = 0.45f),
            radius = bodyW * 0.48f,
            center = Offset(px, py + bodyH * 0.38f)
        )
        // Sweater (Blue + Magenta stripe)
        drawRect(
            color = Color(0xFF3F51B5),
            topLeft = Offset(px - bodyW * 0.42f, py - bodyH * 0.05f),
            size = Size(bodyW * 0.84f, bodyH * 0.45f)
        )
        drawRect(
            color = Color(0xFFD81B60),
            topLeft = Offset(px - bodyW * 0.42f, py + bodyH * 0.10f),
            size = Size(bodyW * 0.84f, bodyH * 0.12f)
        )
        // Head & Hair
        drawRect(
            color = Color(0xFFFFE082),
            topLeft = Offset(px - bodyW * 0.36f, py - bodyH * 0.45f),
            size = Size(bodyW * 0.72f, bodyH * 0.40f)
        )
        drawRect(
            color = Color(0xFF4E342E),
            topLeft = Offset(px - bodyW * 0.40f, py - bodyH * 0.50f),
            size = Size(bodyW * 0.80f, bodyH * 0.18f)
        )
        // Direction indicator eyes
        val eyeOffsetX = when (direction) {
            Direction.LEFT -> -bodyW * 0.12f
            Direction.RIGHT -> bodyW * 0.12f
            else -> 0f
        }
        if (direction != Direction.UP) {
            drawRect(
                color = Color.Black,
                topLeft = Offset(px - bodyW * 0.20f + eyeOffsetX, py - bodyH * 0.28f),
                size = Size(bodyW * 0.12f, bodyH * 0.10f)
            )
            drawRect(
                color = Color.Black,
                topLeft = Offset(px + bodyW * 0.08f + eyeOffsetX, py - bodyH * 0.28f),
                size = Size(bodyW * 0.12f, bodyH * 0.10f)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNpcMarker(
    cx: Float,
    cy: Float,
    tileW: Float,
    primaryColor: Color,
    enemyId: EnemyId,
    spared: Set<EnemyId>,
    killed: Set<EnemyId>
) {
    if (killed.contains(enemyId)) {
        // Pile of dust
        drawCircle(color = SoftGray.copy(alpha = 0.5f), radius = tileW * 0.22f, center = Offset(cx, cy + 4f))
        return
    }
    val isSpared = spared.contains(enemyId)
    drawCircle(
        color = (if (isSpared) MercyYellow else primaryColor).copy(alpha = 0.28f),
        radius = tileW * 0.42f,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = primaryColor,
        radius = tileW * 0.28f,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = if (isSpared) KindnessGreen else SoulRed,
        radius = tileW * 0.10f,
        center = Offset(cx, cy - tileW * 0.34f)
    )
}
