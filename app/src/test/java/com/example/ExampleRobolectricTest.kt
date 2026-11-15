package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.BattleAction
import com.example.model.BattlePhase
import com.example.model.EndingRoute
import com.example.model.EnemyId
import com.example.model.ScreenRoute
import com.example.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Kararlı Ruh", appName)
    }

    @Test
    fun `direct battle ACT increases mercy and allows sparing`() {
        val vm = GameViewModel()
        vm.startDirectBattle(EnemyId.PAPATYA)
        val initial = vm.uiState.value
        assertEquals(ScreenRoute.BATTLE, initial.screen)
        assertNotNull(initial.battleState)

        vm.selectBattleAction(BattleAction.ACT)
        val actState = vm.uiState.value.battleState!!
        assertEquals(BattlePhase.SUBMENU_ACT, actState.phase)

        val compliment = actState.enemy.acts.first { it.id == "compliment" }
        vm.executeAct(compliment)
        assertTrue(vm.uiState.value.battleState!!.mercyProgress >= 50)
    }

    @Test
    fun `story adventure starts in ruins entrance with full hp`() {
        val vm = GameViewModel()
        vm.updatePlayerName("deniz")
        vm.startNewAdventure()
        val state = vm.uiState.value
        assertEquals(ScreenRoute.OVERWORLD, state.screen)
        assertEquals("DENİZ", state.playerName)
        assertEquals(20, state.hp)
        assertEquals(EndingRoute.NEUTRAL, state.endingRoute)
    }
}
