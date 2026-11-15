package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ScreenRoute
import com.example.ui.screens.BattleScreen
import com.example.ui.screens.EndingScreen
import com.example.ui.screens.OverworldScreen
import com.example.ui.screens.TitleScreen
import com.example.ui.theme.KararliRuhTheme
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.BLACK),
            navigationBarStyle = SystemBarStyle.dark(Color.BLACK)
        )
        setContent {
            KararliRuhTheme {
                val gameViewModel: GameViewModel = viewModel()
                val uiState by gameViewModel.uiState.collectAsStateWithLifecycle()

                when (uiState.screen) {
                    ScreenRoute.TITLE -> TitleScreen(
                        uiState = uiState,
                        onNameChange = gameViewModel::updatePlayerName,
                        onStartAdventure = gameViewModel::startNewAdventure,
                        onSelectDirectBattle = gameViewModel::startDirectBattle,
                        onToggleMute = gameViewModel::toggleMute
                    )

                    ScreenRoute.OVERWORLD -> OverworldScreen(
                        uiState = uiState,
                        onMoveDirection = gameViewModel::setOverworldMoveDirection,
                        onInteract = gameViewModel::interactOverworld,
                        onAdvanceDialog = gameViewModel::advanceOverworldDialog,
                        onToggleMute = gameViewModel::toggleMute,
                        onBackToTitle = gameViewModel::returnToTitle
                    )

                    ScreenRoute.BATTLE -> BattleScreen(
                        uiState = uiState,
                        onSelectAction = gameViewModel::selectBattleAction,
                        onBackToMainMenu = gameViewModel::backToBattleMainMenu,
                        onExecuteAct = gameViewModel::executeAct,
                        onUseItem = gameViewModel::useInventoryItem,
                        onAttemptSpare = gameViewModel::attemptSpare,
                        onAttemptFlee = gameViewModel::attemptFlee,
                        onFightStrike = gameViewModel::triggerFightStrike,
                        onMoveSoulNormalized = gameViewModel::moveSoulByNormalizedDelta,
                        onFinishVictory = gameViewModel::finishBattleVictoryOrFlee,
                        onRetryGameOver = gameViewModel::retryAfterGameOver,
                        onToggleMute = gameViewModel::toggleMute,
                        onExitBattleToTitle = gameViewModel::returnToTitle
                    )

                    ScreenRoute.ENDING -> EndingScreen(
                        uiState = uiState,
                        onPlayAgain = gameViewModel::startNewAdventure,
                        onReturnToTitle = gameViewModel::returnToTitle
                    )
                }
            }
        }
    }
}
