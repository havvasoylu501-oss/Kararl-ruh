package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActOption
import com.example.model.BattleAction
import com.example.model.BattlePhase
import com.example.model.BattleState
import com.example.model.BulletShape
import com.example.model.BulletType
import com.example.model.GameUiState
import com.example.ui.sprites.EnemyPixelSprite
import com.example.ui.sprites.PixelHeartIcon
import com.example.ui.sprites.drawHeartInBox
import com.example.ui.theme.ActionOrange
import com.example.ui.theme.BlueSoulCyan
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.HpRed
import com.example.ui.theme.KindnessGreen
import com.example.ui.theme.MercyYellow
import com.example.ui.theme.RuinsBorder
import com.example.ui.theme.RuinsSurface
import com.example.ui.theme.SoftGray
import com.example.ui.theme.SoulRed
import com.example.ui.theme.VoidBlack

@Composable
fun BattleScreen(
    uiState: GameUiState,
    onSelectAction: (BattleAction) -> Unit,
    onBackToMainMenu: () -> Unit,
    onExecuteAct: (ActOption) -> Unit,
    onUseItem: (String) -> Unit,
    onAttemptSpare: () -> Unit,
    onAttemptFlee: () -> Unit,
    onFightStrike: () -> Unit,
    onMoveSoulNormalized: (Float, Float) -> Unit,
    onFinishVictory: () -> Unit,
    onRetryGameOver: () -> Unit,
    onToggleMute: () -> Unit,
    onExitBattleToTitle: () -> Unit
) {
    val battle = uiState.battleState ?: return

    BackHandler {
        if (battle.phase in listOf(
                BattlePhase.SUBMENU_ACT,
                BattlePhase.SUBMENU_ITEM,
                BattlePhase.SUBMENU_MERCY
            )
        ) {
            onBackToMainMenu()
        } else {
            onExitBattleToTitle()
        }
    }

    val isSpareable = battle.mercyProgress >= 100
    var boxPixelSize by remember { mutableStateOf(IntSize(400, 260)) }

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
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Bar: Enemy Name, HP & Mercy Meter, Mute/Exit
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onExitBattleToTitle,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("battle_exit_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Savaştan çık",
                        tint = CrispWhite
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = battle.enemy.name.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSpareable) MercyYellow else CrispWhite
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Enemy HP mini-bar
                        val hpFrac = (battle.enemyHp.toFloat() / battle.enemy.maxHp).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .background(HpRed)
                                .border(1.dp, CrispWhite)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(hpFrac)
                                    .background(KindnessGreen)
                            )
                        }
                        Text(
                            text = "MERHAMET %${battle.mercyProgress}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSpareable) MercyYellow else SoftGray
                        )
                    }
                }
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("battle_mute_button")
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

            // Enemy Stage (Sprite + Speech Bubble)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(DeepObsidian, RoundedCornerShape(4.dp))
                    .border(1.dp, RuinsBorder, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        EnemyPixelSprite(
                            enemyId = battle.enemy.id,
                            isSpareable = isSpareable,
                            isHurt = battle.phase == BattlePhase.PLAYER_SLASH_EFFECT,
                            shakeOffsetX = battle.enemyShakeOffset,
                            size = 118.dp
                        )
                        if (battle.phase == BattlePhase.PLAYER_SLASH_EFFECT) {
                            SlashOverlayCanvas(progress = battle.slashProgress)
                            battle.lastDamageText?.let { dmgText ->
                                Text(
                                    text = dmgText,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = SoulRed,
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .background(VoidBlack.copy(alpha = 0.75f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (battle.phase == BattlePhase.ENEMY_SPEECH && battle.enemySpeechText.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = CrispWhite,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = battle.enemySpeechText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = VoidBlack,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }

            // Middle White-Bordered Undertale Battle Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(VoidBlack, RoundedCornerShape(2.dp))
                    .border(3.dp, CrispWhite, RoundedCornerShape(2.dp))
                    .onSizeChanged { boxPixelSize = it }
            ) {
                when (battle.phase) {
                    BattlePhase.PLAYER_MENU,
                    BattlePhase.ENEMY_SPEECH -> {
                        Text(
                            text = battle.narratorText,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                            color = CrispWhite,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }

                    BattlePhase.SUBMENU_ACT -> {
                        SubMenuHeader(title = "EYLEM SEÇ", onBack = onBackToMainMenu) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(battle.enemy.acts) { act ->
                                    RetroSubButton(
                                        label = "* ${act.label}",
                                        accentColor = CrispWhite,
                                        testTag = "act_${act.id}",
                                        onClick = { onExecuteAct(act) }
                                    )
                                }
                            }
                        }
                    }

                    BattlePhase.SUBMENU_ITEM -> {
                        val usableItems = uiState.inventory.filter { it.count > 0 }
                        SubMenuHeader(title = "EŞYA KULLAN", onBack = onBackToMainMenu) {
                            if (usableItems.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "* Çantan tamamen boş!",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = SoftGray
                                    )
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(usableItems, key = { it.id }) { item ->
                                        RetroSubButton(
                                            label = "* ${item.name} (${item.count}) +${item.healAmount}",
                                            accentColor = KindnessGreen,
                                            testTag = "item_${item.id}",
                                            onClick = { onUseItem(item.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    BattlePhase.SUBMENU_MERCY -> {
                        SubMenuHeader(title = "MERHAMET", onBack = onBackToMainMenu) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                RetroSubButton(
                                    label = if (isSpareable) {
                                        "★ BAĞIŞLA (${battle.enemy.name} hazır!)"
                                    } else {
                                        "* BAĞIŞLA (Henüz ikna olmadı)"
                                    },
                                    accentColor = if (isSpareable) MercyYellow else CrispWhite,
                                    testTag = "mercy_spare_button",
                                    onClick = onAttemptSpare
                                )
                                RetroSubButton(
                                    label = "* KAÇ",
                                    accentColor = SoftGray,
                                    testTag = "mercy_flee_button",
                                    onClick = onAttemptFlee
                                )
                            }
                        }
                    }

                    BattlePhase.FIGHT_TIMING -> {
                        FightTimingArena(
                            cursorPos = battle.fightCursorPos,
                            onStrike = onFightStrike
                        )
                    }

                    BattlePhase.PLAYER_SLASH_EFFECT -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "* SALDIRI!",
                                style = MaterialTheme.typography.titleLarge,
                                color = ActionOrange
                            )
                        }
                    }

                    BattlePhase.ENEMY_ATTACK -> {
                        BulletHellCanvas(
                            battle = battle,
                            boxPixelSize = boxPixelSize,
                            onMoveSoulNormalized = onMoveSoulNormalized
                        )
                    }

                    BattlePhase.VICTORY_SPARED,
                    BattlePhase.VICTORY_KILLED,
                    BattlePhase.FLED -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = battle.narratorText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (battle.phase == BattlePhase.VICTORY_SPARED) {
                                    MercyYellow
                                } else {
                                    CrispWhite
                                }
                            )
                            Button(
                                onClick = onFinishVictory,
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VoidBlack,
                                    contentColor = MercyYellow
                                ),
                                border = BorderStroke(2.dp, MercyYellow),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("battle_continue_button")
                            ) {
                                Text("DEVAM ET ▶", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }

                    BattlePhase.GAME_OVER -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "OYUN BİTTİ",
                                style = MaterialTheme.typography.headlineMedium,
                                color = SoulRed
                            )
                            Text(
                                text = "\"${uiState.playerName}! Kararlılığını kaybetme...\"",
                                style = MaterialTheme.typography.bodyLarge,
                                color = CrispWhite,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = onRetryGameOver,
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VoidBlack,
                                    contentColor = SoulRed
                                ),
                                border = BorderStroke(2.dp, SoulRed),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("battle_retry_button")
                            ) {
                                Text("KARARLI KAL (TEKRAR DENE)", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }

            // Player Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${uiState.playerName}  LV ${uiState.lv}",
                    style = MaterialTheme.typography.labelLarge,
                    color = CrispWhite
                )
                Text(
                    text = "HP",
                    style = MaterialTheme.typography.labelSmall,
                    color = CrispWhite
                )
                val playerHpFrac = (uiState.hp.toFloat() / uiState.maxHp.coerceAtLeast(1)).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp)
                        .background(HpRed)
                        .border(1.dp, CrispWhite)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(playerHpFrac)
                            .background(MercyYellow)
                    )
                }
                Text(
                    text = "${uiState.hp} / ${uiState.maxHp}",
                    style = MaterialTheme.typography.labelLarge,
                    color = CrispWhite
                )
            }

            // Bottom Control Area: During ENEMY_ATTACK show spacious touch-drag pad so finger never covers SOUL!
            if (battle.phase == BattlePhase.ENEMY_ATTACK) {
                Surface(
                    color = DeepObsidian,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(2.dp, BlueSoulCyan.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .pointerInput(boxPixelSize) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val w = boxPixelSize.width.coerceAtLeast(1).toFloat()
                                val h = boxPixelSize.height.coerceAtLeast(1).toFloat()
                                onMoveSoulNormalized(dragAmount.x / w * 1.25f, dragAmount.y / h * 1.25f)
                            }
                        }
                        .testTag("soul_touchpad")
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "RUHU YÖNLENDİRMEK İÇİN PARMAĞINI KAYDIR",
                            style = MaterialTheme.typography.labelLarge,
                            color = BlueSoulCyan
                        )
                        Text(
                            text = "Mavi = DUR  •  Turuncu = HAREKET ET  •  Yeşil = İYİLEŞ",
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftGray
                        )
                    }
                }
            } else {
                // Classic 4 Action Buttons
                val menuEnabled = battle.phase in listOf(
                    BattlePhase.PLAYER_MENU,
                    BattlePhase.SUBMENU_ACT,
                    BattlePhase.SUBMENU_ITEM,
                    BattlePhase.SUBMENU_MERCY
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionCommandButton(
                        label = "SAVAŞ",
                        icon = Icons.Default.FlashOn,
                        active = battle.selectedAction == BattleAction.FIGHT,
                        enabled = menuEnabled,
                        accent = ActionOrange,
                        testTag = "btn_fight",
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectAction(BattleAction.FIGHT) }
                    )
                    ActionCommandButton(
                        label = "EYLEM",
                        icon = Icons.Default.RecordVoiceOver,
                        active = battle.selectedAction == BattleAction.ACT,
                        enabled = menuEnabled,
                        accent = ActionOrange,
                        testTag = "btn_act",
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectAction(BattleAction.ACT) }
                    )
                    ActionCommandButton(
                        label = "EŞYA",
                        icon = Icons.Default.Backpack,
                        active = battle.selectedAction == BattleAction.ITEM,
                        enabled = menuEnabled,
                        accent = ActionOrange,
                        testTag = "btn_item",
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectAction(BattleAction.ITEM) }
                    )
                    ActionCommandButton(
                        label = "MERHAMET",
                        icon = Icons.Default.Favorite,
                        active = battle.selectedAction == BattleAction.MERCY || isSpareable,
                        enabled = menuEnabled,
                        accent = if (isSpareable) MercyYellow else ActionOrange,
                        testTag = "btn_mercy",
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectAction(BattleAction.MERCY) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SubMenuHeader(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = ActionOrange
            )
            Surface(
                color = RuinsSurface,
                border = BorderStroke(1.dp, CrispWhite),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .clickable { onBack() }
                    .testTag("submenu_back_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Geri",
                        tint = CrispWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("GERİ", style = MaterialTheme.typography.labelSmall, color = CrispWhite)
                }
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

@Composable
private fun RetroSubButton(
    label: String,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        color = DeepObsidian,
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.85f)),
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PixelHeartIcon(size = 12.dp, color = SoulRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FightTimingArena(
    cursorPos: Float,
    onStrike: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onStrike() }
            .padding(16.dp)
            .testTag("fight_timing_box"),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TAM ORTADA VURMAK İÇİN DOKUN!",
            style = MaterialTheme.typography.labelLarge,
            color = MercyYellow
        )
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            // Eye / Target Ellipse Brackets
            drawOval(
                color = CrispWhite.copy(alpha = 0.35f),
                topLeft = Offset(size.width * 0.04f, 6f),
                size = Size(size.width * 0.92f, size.height - 12f),
                style = Stroke(width = 3f)
            )
            // Center Sweet Spot Zone
            val centerW = size.width * 0.14f
            drawRect(
                color = KindnessGreen.copy(alpha = 0.45f),
                topLeft = Offset(size.width / 2f - centerW / 2f, 8f),
                size = Size(centerW, size.height - 16f)
            )
            drawLine(
                color = MercyYellow,
                start = Offset(size.width / 2f, 4f),
                end = Offset(size.width / 2f, size.height - 4f),
                strokeWidth = 3f
            )
            // Sweeping Strike Bar
            val barX = cursorPos * (size.width - 14f) + 7f
            drawRect(
                color = CrispWhite,
                topLeft = Offset(barX - 5f, 0f),
                size = Size(10f, size.height)
            )
            drawRect(
                color = ActionOrange,
                topLeft = Offset(barX - 2f, 4f),
                size = Size(4f, size.height - 8f)
            )
        }
        Surface(
            color = SoulRed,
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = "⚔ VUR! ⚔",
                style = MaterialTheme.typography.titleMedium,
                color = CrispWhite,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun BulletHellCanvas(
    battle: BattleState,
    boxPixelSize: IntSize,
    onMoveSoulNormalized: (Float, Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(boxPixelSize) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val w = boxPixelSize.width.coerceAtLeast(1).toFloat()
                    val h = boxPixelSize.height.coerceAtLeast(1).toFloat()
                    onMoveSoulNormalized(dragAmount.x / w, dragAmount.y / h)
                }
            }
            .testTag("bullet_hell_arena")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val minDim = minOf(w, h)

            // Subtle countdown progress bar along top edge
            val timeFrac = (battle.attackTimeLeftSec / battle.enemy.attackDurationSec).coerceIn(0f, 1f)
            drawRect(
                color = MercyYellow.copy(alpha = 0.65f),
                topLeft = Offset.Zero,
                size = Size(w * timeFrac, 5f)
            )

            // Draw Bullets
            for (b in battle.bullets) {
                val bx = b.x * w
                val by = b.y * h
                val color = when (b.type) {
                    BulletType.WHITE -> CrispWhite
                    BulletType.CYAN_STILL -> BlueSoulCyan
                    BulletType.ORANGE_MOVE -> ActionOrange
                    BulletType.GREEN_HEAL -> KindnessGreen
                }

                when (b.shape) {
                    BulletShape.PELLET -> {
                        val r = b.radius * minDim
                        drawCircle(color = color, radius = r, center = Offset(bx, by))
                        drawCircle(color = VoidBlack.copy(alpha = 0.3f), radius = r * 0.4f, center = Offset(bx, by))
                    }
                    BulletShape.STAR -> {
                        val r = b.radius * minDim * 1.15f
                        rotate(degrees = b.rotationDeg, pivot = Offset(bx, by)) {
                            val path = Path().apply {
                                moveTo(bx, by - r)
                                lineTo(bx + r * 0.35f, by - r * 0.35f)
                                lineTo(bx + r, by)
                                lineTo(bx + r * 0.35f, by + r * 0.35f)
                                lineTo(bx, by + r)
                                lineTo(bx - r * 0.35f, by + r * 0.35f)
                                lineTo(bx - r, by)
                                lineTo(bx - r * 0.35f, by - r * 0.35f)
                                close()
                            }
                            drawPath(path = path, color = color)
                        }
                    }
                    BulletShape.BONE_V, BulletShape.BONE_H -> {
                        val bw = b.halfWidth * w * 2f
                        val bh = b.halfHeight * h * 2f
                        drawRect(
                            color = color,
                            topLeft = Offset(bx - bw / 2f, by - bh / 2f),
                            size = Size(bw, bh)
                        )
                        // Bone knobs
                        val knobR = minOf(bw, bh) * 0.55f
                        if (b.shape == BulletShape.BONE_V) {
                            drawCircle(color = color, radius = knobR, center = Offset(bx, by - bh / 2f))
                            drawCircle(color = color, radius = knobR, center = Offset(bx, by + bh / 2f))
                        } else {
                            drawCircle(color = color, radius = knobR, center = Offset(bx - bw / 2f, by))
                            drawCircle(color = color, radius = knobR, center = Offset(bx + bw / 2f, by))
                        }
                    }
                    BulletShape.SPEAR -> {
                        val bw = b.halfWidth * w * 2f
                        val bh = b.halfHeight * h * 2f
                        rotate(degrees = b.rotationDeg, pivot = Offset(bx, by)) {
                            drawRect(
                                color = color,
                                topLeft = Offset(bx - bw / 2f, by - bh / 2f),
                                size = Size(bw, bh)
                            )
                            val tip = Path().apply {
                                moveTo(bx + bw / 2f + bh, by)
                                lineTo(bx + bw / 2f, by - bh * 0.9f)
                                lineTo(bx + bw / 2f, by + bh * 0.9f)
                                close()
                            }
                            drawPath(path = tip, color = color)
                        }
                    }
                }
            }

            // Draw SOUL Heart (blinks during i-frames)
            val blinkVisible = battle.invulnerableSec <= 0f || ((battle.invulnerableSec * 20).toInt() % 2 == 0)
            if (blinkVisible) {
                drawHeartInBox(
                    centerX = battle.soulX * w,
                    centerY = battle.soulY * h,
                    radiusPx = 0.032f * minDim,
                    color = SoulRed
                )
            }
        }
    }
}

@Composable
private fun SlashOverlayCanvas(progress: Float) {
    Canvas(modifier = Modifier.size(110.dp)) {
        val endX = size.width * progress.coerceIn(0.1f, 1f)
        val endY = size.height * progress.coerceIn(0.1f, 1f)
        drawLine(
            color = SoulRed,
            start = Offset(10f, 10f),
            end = Offset(endX, endY),
            strokeWidth = 10f
        )
        drawLine(
            color = CrispWhite,
            start = Offset(14f, 14f),
            end = Offset(endX * 0.9f, endY * 0.9f),
            strokeWidth = 4f
        )
    }
}

@Composable
private fun ActionCommandButton(
    label: String,
    icon: ImageVector,
    active: Boolean,
    enabled: Boolean,
    accent: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (active) MercyYellow else accent
    val contentColor = if (active) MercyYellow else accent
    Surface(
        color = VoidBlack,
        border = BorderStroke(2.dp, if (enabled) borderColor else RuinsBorder),
        shape = RoundedCornerShape(2.dp),
        modifier = modifier
            .height(54.dp)
            .clickable(enabled = enabled) { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) contentColor else SoftGray,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (enabled) contentColor else SoftGray,
                maxLines = 1
            )
        }
    }
}
