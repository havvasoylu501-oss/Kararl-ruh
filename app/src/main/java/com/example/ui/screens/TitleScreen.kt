package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.GameContent
import com.example.model.EnemyId
import com.example.model.GameUiState
import com.example.ui.sprites.EnemyPixelSprite
import com.example.ui.sprites.PixelHeartIcon
import com.example.ui.sprites.SaveStarIcon
import com.example.ui.theme.ActionOrange
import com.example.ui.theme.BlueSoulCyan
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.KindnessGreen
import com.example.ui.theme.MercyYellow
import com.example.ui.theme.RuinsBorder
import com.example.ui.theme.RuinsSurface
import com.example.ui.theme.SoftGray
import com.example.ui.theme.SoulRed
import com.example.ui.theme.VoidBlack

@Composable
fun TitleScreen(
    uiState: GameUiState,
    onNameChange: (String) -> Unit,
    onStartAdventure: () -> Unit,
    onSelectDirectBattle: (EnemyId) -> Unit,
    onToggleMute: () -> Unit
) {
    val scroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Atmospheric Ruins Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_ruins_banner_1794767796230),
                    contentDescription = "Yeraltı harabeleri sahne görseli",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.25f),
                                    Color.Black.copy(alpha = 0.45f),
                                    VoidBlack
                                )
                            )
                        )
                )

                // Top Mute Toggle
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(48.dp)
                        .background(VoidBlack.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                        .border(1.dp, CrispWhite.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .testTag("title_mute_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isMuted) {
                            Icons.AutoMirrored.Filled.VolumeOff
                        } else {
                            Icons.AutoMirrored.Filled.VolumeUp
                        },
                        contentDescription = if (uiState.isMuted) "Sesi aç" else "Sesi kapat",
                        tint = CrispWhite
                    )
                }

                // Title Overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PixelHeartIcon(size = 24.dp, color = SoulRed)
                        Text(
                            text = "KARARLI RUH",
                            style = MaterialTheme.typography.displayLarge,
                            color = CrispWhite,
                            textAlign = TextAlign.Center
                        )
                        SaveStarIcon(size = 24.dp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "* Savaş ya da Bağışla. Seçim senin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MercyYellow
                    )
                }
            }

            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Player Name Card
                Surface(
                    color = DeepObsidian,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(2.dp, CrispWhite)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "* DÜŞEN İNSANA BİR İSİM VER:",
                            style = MaterialTheme.typography.labelLarge,
                            color = CrispWhite
                        )
                        OutlinedTextField(
                            value = uiState.playerName,
                            onValueChange = onNameChange,
                            singleLine = true,
                            placeholder = { Text("FRİSK", color = SoftGray) },
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                color = MercyYellow,
                                fontSize = 18.sp
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ActionOrange,
                                unfocusedBorderColor = RuinsBorder,
                                cursorColor = MercyYellow,
                                focusedContainerColor = VoidBlack,
                                unfocusedContainerColor = VoidBlack
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("player_name_input")
                        )
                    }
                }

                // Start Adventure Button
                Button(
                    onClick = onStartAdventure,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VoidBlack,
                        contentColor = ActionOrange
                    ),
                    border = BorderStroke(2.dp, ActionOrange),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("start_adventure_button")
                ) {
                    PixelHeartIcon(size = 20.dp, color = SoulRed)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "MACERAYA BAŞLA (HİKAYE)",
                        style = MaterialTheme.typography.titleLarge,
                        color = ActionOrange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = ActionOrange
                    )
                }

                // Mechanics Quick Guide
                Surface(
                    color = RuinsSurface,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, RuinsBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LegendPill(color = BlueSoulCyan, label = "MAVİ: DUR")
                        LegendPill(color = ActionOrange, label = "TURUNCU: KOŞ")
                        LegendPill(color = KindnessGreen, label = "YEŞİL: +HP")
                    }
                }

                // Direct Boss / Encounter Select
                Text(
                    text = "HIZLI SAVAŞ ARENASI",
                    style = MaterialTheme.typography.titleLarge,
                    color = CrispWhite,
                    modifier = Modifier.padding(top = 4.dp)
                )

                EnemyId.entries.forEach { enemyId ->
                    val enemy = GameContent.enemies[enemyId] ?: return@forEach
                    val isSpared = uiState.sparedEnemies.contains(enemyId)
                    val isKilled = uiState.killedEnemies.contains(enemyId)
                    Surface(
                        color = DeepObsidian,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(
                            2.dp,
                            when {
                                isSpared -> MercyYellow
                                isKilled -> SoulRed
                                else -> CrispWhite.copy(alpha = 0.7f)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onSelectDirectBattle(enemyId) }
                            .testTag("arena_enemy_${enemyId.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(VoidBlack, RoundedCornerShape(4.dp))
                                    .border(1.dp, RuinsBorder, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                EnemyPixelSprite(
                                    enemyId = enemyId,
                                    isSpareable = isSpared,
                                    isHurt = false,
                                    shakeOffsetX = 0f,
                                    size = 44.dp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = enemy.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isSpared) MercyYellow else CrispWhite,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = enemy.subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SoftGray
                                )
                                Text(
                                    text = "HP ${enemy.maxHp}  •  ATK ${enemy.atk}  •  DEF ${enemy.def}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ActionOrange
                                )
                            }
                            Text(
                                text = when {
                                    isSpared -> "BAĞIŞLANDI ★"
                                    isKilled -> "YENİLDİ"
                                    else -> "SAVAŞ ▶"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = when {
                                    isSpared -> MercyYellow
                                    isKilled -> SoulRed
                                    else -> CrispWhite
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun LegendPill(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
