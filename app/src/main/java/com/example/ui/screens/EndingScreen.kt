package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.GameContent
import com.example.model.EndingRoute
import com.example.model.EnemyId
import com.example.model.GameUiState
import com.example.ui.sprites.PixelHeartIcon
import com.example.ui.sprites.SaveStarIcon
import com.example.ui.theme.ActionOrange
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.KindnessGreen
import com.example.ui.theme.MercyYellow
import com.example.ui.theme.RuinsBorder
import com.example.ui.theme.SoftGray
import com.example.ui.theme.SoulRed
import com.example.ui.theme.VoidBlack

@Composable
fun EndingScreen(
    uiState: GameUiState,
    onPlayAgain: () -> Unit,
    onReturnToTitle: () -> Unit
) {
    BackHandler {
        onReturnToTitle()
    }

    val route = uiState.endingRoute
    val (title, subtitle, bodyColor) = when (route) {
        EndingRoute.TRUE_PACIFIST -> Triple(
            "GERÇEK BARIŞÇIL SON",
            "* Kimseyi incitmedin! Yeraltı'ndaki tüm canavarlar merhametini şarkılarla anıyor.",
            MercyYellow
        )
        EndingRoute.NEUTRAL -> Triple(
            "NÖTR SON",
            "* Bariyeri aşıp yüzeye çıktın, ama arkanda bıraktığın izler hâlâ tartışılıyor.",
            ActionOrange
        )
        EndingRoute.NO_MERCY -> Triple(
            "KARANLIK YOL",
            "* Herkesi yendin ve gücün zirvesine çıktın... Ama taht salonunda sadece sessizlik var.",
            SoulRed
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 580.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SaveStarIcon(size = 32.dp)
                PixelHeartIcon(size = 32.dp, color = bodyColor)
                SaveStarIcon(size = 32.dp)
            }

            Text(
                text = title,
                style = MaterialTheme.typography.displayLarge,
                color = bodyColor,
                textAlign = TextAlign.Center
            )

            Surface(
                color = DeepObsidian,
                border = BorderStroke(2.dp, CrispWhite),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = CrispWhite,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Surface(
                color = DeepObsidian,
                border = BorderStroke(1.5.dp, RuinsBorder),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "YOLCULUK ÖZETİ — ${uiState.playerName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MercyYellow
                    )
                    Text(
                        text = "Seviye (LV): ${uiState.lv}   •   Altın: ${uiState.gold}G   •   Can: ${uiState.hp}/${uiState.maxHp}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CrispWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    EnemyId.entries.forEach { enemyId ->
                        val name = GameContent.enemies[enemyId]?.name ?: enemyId.name
                        val status = when {
                            uiState.sparedEnemies.contains(enemyId) -> "BAĞIŞLANDI ★" to KindnessGreen
                            uiState.killedEnemies.contains(enemyId) -> "YENİLDİ ⚔" to SoulRed
                            else -> "KARŞILAŞILMADI" to SoftGray
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "* $name",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CrispWhite
                            )
                            Text(
                                text = status.first,
                                style = MaterialTheme.typography.labelLarge,
                                color = status.second
                            )
                        }
                    }
                }
            }

            Button(
                onClick = onPlayAgain,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VoidBlack,
                    contentColor = MercyYellow
                ),
                border = BorderStroke(2.dp, MercyYellow),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("ending_replay_button")
            ) {
                PixelHeartIcon(size = 18.dp, color = SoulRed)
                Spacer(modifier = Modifier.width(10.dp))
                Text("YENİDEN OYNA", style = MaterialTheme.typography.titleMedium)
            }

            Button(
                onClick = onReturnToTitle,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VoidBlack,
                    contentColor = CrispWhite
                ),
                border = BorderStroke(2.dp, CrispWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("ending_title_button")
            ) {
                Text("ANA MENÜ", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
