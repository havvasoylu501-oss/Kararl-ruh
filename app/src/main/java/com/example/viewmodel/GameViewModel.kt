package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEngine
import com.example.data.GameContent
import com.example.engine.BulletPatterns
import com.example.model.ActOption
import com.example.model.BattleAction
import com.example.model.BattlePhase
import com.example.model.BattleState
import com.example.model.Bullet
import com.example.model.BulletShape
import com.example.model.BulletType
import com.example.model.Direction
import com.example.model.EnemyId
import com.example.model.GameUiState
import com.example.model.OverworldDialog
import com.example.model.OverworldRoom
import com.example.model.ScreenRoute
import com.example.model.TileType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

class GameViewModel : ViewModel() {

    private val soundEngine = SoundEngine()

    private val _uiState = MutableStateFlow(
        GameUiState(inventory = GameContent.initialInventory)
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var typewriterJob: Job? = null
    private var battleLoopJob: Job? = null
    private var overworldMoveJob: Job? = null
    private var heldDirection: Direction? = null
    private var lastSoulMoveTimeMs: Long = 0L

    fun toggleMute() {
        val next = !_uiState.value.isMuted
        soundEngine.isMuted = next
        _uiState.update { it.copy(isMuted = next) }
        if (!next) soundEngine.playMenuSelect()
    }

    fun updatePlayerName(name: String) {
        val sanitized = name.uppercase(java.util.Locale.forLanguageTag("tr")).take(10)
        _uiState.update { it.copy(playerName = sanitized) }
    }

    fun startNewAdventure() {
        soundEngine.playSaveStar()
        val chosenName = _uiState.value.playerName.ifBlank { "FRİSK" }
        _uiState.value = GameUiState(
            screen = ScreenRoute.OVERWORLD,
            playerName = chosenName,
            hp = 20,
            maxHp = 20,
            lv = 1,
            xp = 0,
            gold = 15,
            inventory = GameContent.initialInventory,
            currentRoom = OverworldRoom.RUINS_ENTRANCE,
            playerX = 5.5f,
            playerY = 7.5f,
            playerDirection = Direction.UP,
            isMuted = _uiState.value.isMuted,
            overworldDialog = OverworldDialog(
                speaker = "ANLATICI",
                lines = listOf(
                    "* Uzun zaman önce, Dünya'yı iki ırk yönetirdi: İNSANLAR ve CANAVARLAR...",
                    "* Şimdi Yeraltı'nın Harabelerindesin. Yön tuşlarıyla yürü, İNCELE ile konuş!"
                ),
                voicePitchHz = 420f
            )
        )
        startDialogTypewriter()
    }

    fun startDirectBattle(enemyId: EnemyId) {
        val chosenName = _uiState.value.playerName.ifBlank { "FRİSK" }
        if (_uiState.value.inventory.isEmpty()) {
            _uiState.update {
                it.copy(
                    playerName = chosenName,
                    inventory = GameContent.initialInventory
                )
            }
        } else {
            _uiState.update { it.copy(playerName = chosenName) }
        }
        launchBattle(enemyId = enemyId, isFromOverworld = false)
    }

    fun returnToTitle() {
        stopAllLoops()
        soundEngine.playMenuMove()
        _uiState.update {
            it.copy(
                screen = ScreenRoute.TITLE,
                battleState = null,
                overworldDialog = null
            )
        }
    }

    // ---------------- OVERWORLD ----------------

    fun setOverworldMoveDirection(dir: Direction?) {
        heldDirection = dir
        if (dir == null) {
            overworldMoveJob?.cancel()
            _uiState.update { it.copy(isMovingOverworld = false) }
            return
        }
        if (_uiState.value.overworldDialog != null || _uiState.value.screen != ScreenRoute.OVERWORLD) {
            return
        }
        if (overworldMoveJob?.isActive == true) return
        overworldMoveJob = viewModelScope.launch {
            while (heldDirection != null && _uiState.value.screen == ScreenRoute.OVERWORLD && _uiState.value.overworldDialog == null) {
                val activeDir = heldDirection ?: break
                stepOverworld(activeDir, 0.14f)
                delay(16L)
            }
            _uiState.update { it.copy(isMovingOverworld = false) }
        }
    }

    private fun stepOverworld(dir: Direction, speed: Float) {
        val state = _uiState.value
        val roomDef = GameContent.rooms[state.currentRoom] ?: return
        val dx = when (dir) {
            Direction.LEFT -> -speed
            Direction.RIGHT -> speed
            else -> 0f
        }
        val dy = when (dir) {
            Direction.UP -> -speed
            Direction.DOWN -> speed
            else -> 0f
        }
        val targetX = state.playerX + dx
        val targetY = state.playerY + dy
        val tileX = targetX.toInt().coerceIn(0, 11)
        val tileY = targetY.toInt().coerceIn(0, 9)
        val ch = roomDef.grid.getOrNull(tileY)?.getOrNull(tileX) ?: '#'
        val tile = GameContent.parseTile(ch, state.switchPressed)

        val blocked = when (tile) {
            TileType.WALL,
            TileType.SPIKES_CLOSED,
            TileType.SAVE_STAR,
            TileType.SIGN,
            TileType.CANDY_BOWL -> true
            TileType.NPC_PAPATYA -> !state.resolvedEnemies.contains(EnemyId.PAPATYA)
            TileType.NPC_ZIPLAK -> !state.resolvedEnemies.contains(EnemyId.ZIPLAK)
            TileType.NPC_KEMIK -> !state.resolvedEnemies.contains(EnemyId.KEMIK_SOVALYE)
            TileType.NPC_VEX -> !state.resolvedEnemies.contains(EnemyId.GOLGE_VEX)
            else -> false
        }

        if (blocked) {
            _uiState.update { it.copy(playerDirection = dir, isMovingOverworld = false) }
            return
        }

        // Handle floor triggers
        var switchPressedNow = state.switchPressed
        var statusMsg: String? = null
        if (tile == TileType.SWITCH_PLATE && !state.switchPressed) {
            switchPressedNow = true
            soundEngine.playSaveStar()
            statusMsg = "* Klik! Dikenler geri çekildi!"
        }

        if (tile == TileType.PORTAL_NEXT) {
            moveRoom(forward = true)
            return
        }
        if (tile == TileType.PORTAL_PREV) {
            moveRoom(forward = false)
            return
        }

        _uiState.update {
            it.copy(
                playerX = targetX,
                playerY = targetY,
                playerDirection = dir,
                isMovingOverworld = true,
                switchPressed = switchPressedNow,
                statusToast = statusMsg ?: it.statusToast
            )
        }
    }

    private fun moveRoom(forward: Boolean) {
        val state = _uiState.value
        val order = OverworldRoom.entries
        val idx = order.indexOf(state.currentRoom)
        val nextIdx = if (forward) idx + 1 else idx - 1
        if (nextIdx !in order.indices) return
        val nextRoom = order[nextIdx]
        val roomDef = GameContent.rooms[nextRoom] ?: return
        val spawn = if (forward) roomDef.spawnFromPrev else roomDef.spawnFromNext
        soundEngine.playMenuMove()
        _uiState.update {
            it.copy(
                currentRoom = nextRoom,
                playerX = spawn.first,
                playerY = spawn.second,
                isMovingOverworld = false,
                statusToast = null
            )
        }
    }

    fun interactOverworld() {
        val state = _uiState.value
        if (state.screen != ScreenRoute.OVERWORLD) return
        if (state.overworldDialog != null) {
            advanceOverworldDialog()
            return
        }

        val roomDef = GameContent.rooms[state.currentRoom] ?: return
        val lookOffset = when (state.playerDirection) {
            Direction.UP -> 0f to -0.85f
            Direction.DOWN -> 0f to 0.85f
            Direction.LEFT -> -0.85f to 0f
            Direction.RIGHT -> 0.85f to 0f
        }
        val checkPoints = listOf(
            state.playerX + lookOffset.first to state.playerY + lookOffset.second,
            state.playerX to state.playerY
        )

        var foundTile: TileType? = null
        for ((cx, cy) in checkPoints) {
            val tx = cx.toInt().coerceIn(0, 11)
            val ty = cy.toInt().coerceIn(0, 9)
            val ch = roomDef.grid.getOrNull(ty)?.getOrNull(tx) ?: '.'
            val t = GameContent.parseTile(ch, state.switchPressed)
            if (t in listOf(
                    TileType.SAVE_STAR,
                    TileType.SIGN,
                    TileType.CANDY_BOWL,
                    TileType.NPC_PAPATYA,
                    TileType.NPC_ZIPLAK,
                    TileType.NPC_KEMIK,
                    TileType.NPC_VEX
                )
            ) {
                foundTile = t
                break
            }
        }

        if (foundTile == null) {
            // Also check adjacent 1 tile radius for forgiving mobile tap
            val px = state.playerX.toInt()
            val py = state.playerY.toInt()
            loop@ for (dy in -1..1) {
                for (dx in -1..1) {
                    val ch = roomDef.grid.getOrNull((py + dy).coerceIn(0, 9))
                        ?.getOrNull((px + dx).coerceIn(0, 11)) ?: '.'
                    val t = GameContent.parseTile(ch, state.switchPressed)
                    if (t in listOf(
                            TileType.SAVE_STAR,
                            TileType.SIGN,
                            TileType.CANDY_BOWL,
                            TileType.NPC_PAPATYA,
                            TileType.NPC_ZIPLAK,
                            TileType.NPC_KEMIK,
                            TileType.NPC_VEX
                        )
                    ) {
                        foundTile = t
                        break@loop
                    }
                }
            }
        }

        when (foundTile) {
            TileType.SAVE_STAR -> {
                soundEngine.playSaveStar()
                val quote = when (state.currentRoom) {
                    OverworldRoom.RUINS_ENTRANCE -> "* Harabelerin kadim ışığı parlıyor. Canın tamamen yenilendi!\n* İçinde KARARLILIK doluyor."
                    OverworldRoom.SNOWY_OUTPOST -> "* Kar taneleri atkına konuyor, sıcak bir spagetti kokusu geliyor...\n* KARARLILIK ile doldun! (HP Tam)"
                    else -> "* Çekirdek Tahtı'nın önündesin. Yolculuğunun sonu yaklaşıyor.\n* Tüm canın yenilendi. KARARLILIK!"
                }
                _uiState.update {
                    it.copy(
                        hp = it.maxHp,
                        overworldDialog = OverworldDialog(
                            speaker = "KAYIT YILDIZI ★",
                            lines = listOf(quote, "* Oyun durumu kaydedildi."),
                            voicePitchHz = 700f,
                            healsPlayer = true
                        )
                    )
                }
                startDialogTypewriter()
            }

            TileType.SIGN -> {
                val signLines = when (state.currentRoom) {
                    OverworldRoom.RUINS_ENTRANCE -> listOf(
                        "* TABELA: 'Düşmanları yenmek zorunda değilsin!'",
                        "* 'EYLEM menüsünü kullan ve isimleri SARI olduğunda MERHAMET ile BAĞIŞLA.'"
                    )
                    OverworldRoom.MOSSY_CORRIDOR -> listOf(
                        "* TABELA: 'Sağ üstteki altın plakaya basarsan dikenli kapı açılır.'",
                        "* 'Şeker kasesinden sadece BİR tane alman rica olunur.'"
                    )
                    OverworldRoom.SNOWY_OUTPOST -> listOf(
                        "* TABELA: 'MAVİ saldırılarda HAREKETSİZ KAL. TURUNCU saldırılarda HAREKET ET!'",
                        "* 'YEŞİL küreler ruhunu iyileştirir.'"
                    )
                    OverworldRoom.THRONE_HALL -> listOf(
                        "* TABELA: 'İleride Gölge Şövalyesi Vex bekliyor. Seçimlerin sonunu belirleyecek.'"
                    )
                }
                _uiState.update {
                    it.copy(
                        overworldDialog = OverworldDialog(
                            speaker = "TAŞ TABELA",
                            lines = signLines,
                            voicePitchHz = 360f
                        )
                    )
                }
                startDialogTypewriter()
            }

            TileType.CANDY_BOWL -> {
                val count = state.candyTakenCount
                if (count == 0) {
                    soundEngine.playHeal()
                    val updatedInv = state.inventory.map {
                        if (it.id == "monster_candy") it.copy(count = it.count + 1) else it
                    }
                    _uiState.update {
                        it.copy(
                            candyTakenCount = 1,
                            inventory = updatedInv,
                            overworldDialog = OverworldDialog(
                                speaker = "ŞEKER KASESİ",
                                lines = listOf("* Kaseden bir Canavar Şekeri aldın. (+1 Eşya)"),
                                voicePitchHz = 520f
                            )
                        )
                    }
                } else if (count == 1) {
                    soundEngine.playMenuMove()
                    val updatedInv = state.inventory.map {
                        if (it.id == "monster_candy") it.copy(count = it.count + 1) else it
                    }
                    _uiState.update {
                        it.copy(
                            candyTakenCount = 2,
                            inventory = updatedInv,
                            overworldDialog = OverworldDialog(
                                speaker = "ŞEKER KASESİ",
                                lines = listOf(
                                    "* Bir tane daha aldın...",
                                    "* Kendini biraz bencil hissediyorsun ama şeker lezzetli."
                                ),
                                voicePitchHz = 480f
                            )
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            overworldDialog = OverworldDialog(
                                speaker = "ŞEKER KASESİ",
                                lines = listOf("* Çok fazla aldığın için kase yere devrildi! Artık boş."),
                                voicePitchHz = 320f
                            )
                        )
                    }
                }
                startDialogTypewriter()
            }

            TileType.NPC_PAPATYA -> triggerNpcInteraction(
                enemyId = EnemyId.PAPATYA,
                speaker = "PAPATYA",
                sparedText = "* Papatya: 'Hâlâ buralarda mısın? Suladığın için teşekkürler, küçük ruh!'",
                battleIntro = listOf(
                    "* Merhaba! Ben PAPATYA, küçük bir çiçek!",
                    "* Yeraltı'na yeni düştün değil mi? Sana RUHUNUN nasıl çalıştığını göstereyim!"
                ),
                pitch = 620f
            )

            TileType.NPC_ZIPLAK -> triggerNpcInteraction(
                enemyId = EnemyId.ZIPLAK,
                speaker = "ZIPLAK",
                sparedText = "* Zıplak: 'Vırak! En iyi arkadaşım geri gelmiş!'",
                battleIntro = listOf(
                    "* Vırak... Koridordan geçmek mi istiyorsun?",
                    "* Benimle biraz vakit geçirirsen yolu açabilirim... vırak!"
                ),
                pitch = 290f
            )

            TileType.NPC_KEMIK -> triggerNpcInteraction(
                enemyId = EnemyId.KEMIK_SOVALYE,
                speaker = "ŞÖVALYE KEMİK",
                sparedText = "* Şövalye Kemik: 'NYEH HEH HEH! Spagetti hazır olduğunda seni çağıracağım DOSTUM!'",
                battleIntro = listOf(
                    "* DUR BAKALIM İNSAN! Ben BÜYÜK ŞÖVALYE KEMİK!",
                    "* Karlı Nöbet Yeri'nden geçmeden önce muhteşem bulmacamla—yani savaşla—yüzleş!"
                ),
                pitch = 490f
            )

            TileType.NPC_VEX -> triggerNpcInteraction(
                enemyId = EnemyId.GOLGE_VEX,
                speaker = "GÖLGE ŞÖVALYESİ VEX",
                sparedText = "* Vex: 'Tahtın kapısı senin için açık, kararlı ruh.'",
                battleIntro = listOf(
                    "* Sonunda buraya kadar ulaştın, ${state.playerName}...",
                    "* Yaptığın her seçim bu an için bir sınavdı. Ruhunu savun!"
                ),
                pitch = 210f
            )

            else -> {
                _uiState.update {
                    it.copy(
                        overworldDialog = OverworldDialog(
                            speaker = "ANLATICI",
                            lines = listOf("* Etraf sessiz. Yeraltı taşlarından hafif bir rüzgar esiyor."),
                            voicePitchHz = 400f
                        )
                    )
                }
                startDialogTypewriter()
            }
        }
    }

    private fun triggerNpcInteraction(
        enemyId: EnemyId,
        speaker: String,
        sparedText: String,
        battleIntro: List<String>,
        pitch: Float
    ) {
        val state = _uiState.value
        val dialog = if (state.sparedEnemies.contains(enemyId)) {
            OverworldDialog(speaker = speaker, lines = listOf(sparedText), voicePitchHz = pitch)
        } else if (state.killedEnemies.contains(enemyId)) {
            OverworldDialog(
                speaker = "ANLATICI",
                lines = listOf("* Sadece yerdeki ince toz taneleri kaldı..."),
                voicePitchHz = 280f
            )
        } else {
            OverworldDialog(
                speaker = speaker,
                lines = battleIntro,
                triggerBattle = enemyId,
                voicePitchHz = pitch
            )
        }
        _uiState.update { it.copy(overworldDialog = dialog) }
        startDialogTypewriter()
    }

    private fun startDialogTypewriter() {
        typewriterJob?.cancel()
        val dialog = _uiState.value.overworldDialog ?: return
        val fullLine = dialog.lines.getOrNull(dialog.currentLineIndex) ?: ""
        _uiState.update { it.copy(visibleDialogChars = 0) }
        typewriterJob = viewModelScope.launch {
            for (i in 1..fullLine.length) {
                _uiState.update { it.copy(visibleDialogChars = i) }
                if (i % 3 == 1 && fullLine[i - 1] != ' ') {
                    soundEngine.playBlip(dialog.voicePitchHz)
                }
                delay(22L)
            }
        }
    }

    fun advanceOverworldDialog() {
        val state = _uiState.value
        val dialog = state.overworldDialog ?: return
        val currentLine = dialog.lines.getOrNull(dialog.currentLineIndex) ?: ""
        if (state.visibleDialogChars < currentLine.length) {
            typewriterJob?.cancel()
            _uiState.update { it.copy(visibleDialogChars = currentLine.length) }
            return
        }

        if (dialog.currentLineIndex + 1 < dialog.lines.size) {
            soundEngine.playMenuMove()
            _uiState.update {
                it.copy(
                    overworldDialog = dialog.copy(currentLineIndex = dialog.currentLineIndex + 1)
                )
            }
            startDialogTypewriter()
        } else {
            typewriterJob?.cancel()
            val battleTarget = dialog.triggerBattle
            _uiState.update { it.copy(overworldDialog = null, visibleDialogChars = 0) }
            if (battleTarget != null) {
                launchBattle(battleTarget, isFromOverworld = true)
            }
        }
    }

    // ---------------- BATTLE SYSTEM ----------------

    private fun launchBattle(enemyId: EnemyId, isFromOverworld: Boolean) {
        stopAllLoops()
        soundEngine.playEncounter()
        val enemy = GameContent.enemies[enemyId] ?: return
        val battle = BattleState(
            enemy = enemy,
            enemyHp = enemy.maxHp,
            narratorText = enemy.encounterFlavor,
            isFromOverworld = isFromOverworld
        )
        _uiState.update {
            it.copy(
                screen = ScreenRoute.BATTLE,
                battleState = battle
            )
        }
    }

    fun selectBattleAction(action: BattleAction) {
        val battle = _uiState.value.battleState ?: return
        if (battle.phase != BattlePhase.PLAYER_MENU &&
            battle.phase != BattlePhase.SUBMENU_ACT &&
            battle.phase != BattlePhase.SUBMENU_ITEM &&
            battle.phase != BattlePhase.SUBMENU_MERCY
        ) return

        soundEngine.playMenuSelect()
        val nextPhase = when (action) {
            BattleAction.FIGHT -> BattlePhase.FIGHT_TIMING
            BattleAction.ACT -> BattlePhase.SUBMENU_ACT
            BattleAction.ITEM -> BattlePhase.SUBMENU_ITEM
            BattleAction.MERCY -> BattlePhase.SUBMENU_MERCY
        }

        _uiState.update {
            it.copy(
                battleState = battle.copy(
                    selectedAction = action,
                    phase = nextPhase,
                    fightCursorPos = 0f,
                    fightCursorDirection = 1f
                )
            )
        }

        if (nextPhase == BattlePhase.FIGHT_TIMING) {
            startFightTimingSweep()
        }
    }

    fun backToBattleMainMenu() {
        val battle = _uiState.value.battleState ?: return
        if (battle.phase in listOf(
                BattlePhase.SUBMENU_ACT,
                BattlePhase.SUBMENU_ITEM,
                BattlePhase.SUBMENU_MERCY
            )
        ) {
            soundEngine.playMenuMove()
            _uiState.update {
                it.copy(battleState = battle.copy(phase = BattlePhase.PLAYER_MENU))
            }
        }
    }

    private fun startFightTimingSweep() {
        battleLoopJob?.cancel()
        battleLoopJob = viewModelScope.launch {
            while (true) {
                val b = _uiState.value.battleState ?: break
                if (b.phase != BattlePhase.FIGHT_TIMING) break
                var nextPos = b.fightCursorPos + b.fightCursorDirection * 0.028f
                var nextDir = b.fightCursorDirection
                if (nextPos >= 1f) {
                    nextPos = 1f
                    nextDir = -1f
                } else if (nextPos <= 0f) {
                    nextPos = 0f
                    nextDir = 1f
                }
                _uiState.update {
                    it.copy(
                        battleState = b.copy(
                            fightCursorPos = nextPos,
                            fightCursorDirection = nextDir
                        )
                    )
                }
                delay(16L)
            }
        }
    }

    fun triggerFightStrike() {
        val state = _uiState.value
        val battle = state.battleState ?: return
        if (battle.phase != BattlePhase.FIGHT_TIMING) return
        battleLoopJob?.cancel()

        // Accuracy: 0.5f is dead center!
        val distFromCenter = abs(battle.fightCursorPos - 0.5f) * 2f // 0..1
        val accuracy = (1f - distFromCenter).coerceIn(0.15f, 1f)
        val baseAttack = 14 + (state.lv - 1) * 6
        val rawDmg = ((baseAttack - battle.enemy.def * 0.6f) * (0.65f + accuracy * 1.15f))
            .roundToInt()
            .coerceAtLeast(5)
        val isCrit = distFromCenter < 0.14f
        val newEnemyHp = (battle.enemyHp - rawDmg).coerceAtLeast(0)

        soundEngine.playSlash()

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    battleState = battle.copy(
                        phase = BattlePhase.PLAYER_SLASH_EFFECT,
                        lastDamageText = if (isCrit) "KRİTİK! -$rawDmg" else "-$rawDmg",
                        enemyHp = newEnemyHp,
                        slashProgress = 0.1f,
                        enemyShakeOffset = 18f
                    )
                )
            }
            delay(110L)
            soundEngine.playHit()
            for (step in 1..6) {
                val shake = if (step % 2 == 0) 14f - step * 2f else -(14f - step * 2f)
                val cur = _uiState.value.battleState ?: break
                _uiState.update {
                    it.copy(
                        battleState = cur.copy(
                            slashProgress = (step / 6f),
                            enemyShakeOffset = shake
                        )
                    )
                }
                delay(65L)
            }

            val afterHit = _uiState.value.battleState ?: return@launch
            if (newEnemyHp <= 0) {
                // Enemy defeated! Gain XP / LV / Gold
                val curState = _uiState.value
                val newXp = curState.xp + battle.enemy.xpReward
                val newLv = 1 + (newXp / 35)
                val newMaxHp = 20 + (newLv - 1) * 4
                val newGold = curState.gold + battle.enemy.goldReward
                _uiState.update {
                    it.copy(
                        xp = newXp,
                        lv = newLv,
                        maxHp = newMaxHp,
                        hp = it.hp.coerceAtMost(newMaxHp),
                        gold = newGold,
                        killedEnemies = it.killedEnemies + battle.enemy.id,
                        battleState = afterHit.copy(
                            phase = BattlePhase.VICTORY_KILLED,
                            enemyShakeOffset = 0f,
                            lastDamageText = null,
                            narratorText = "* KAZANDIN! ${battle.enemy.xpReward} XP ve ${battle.enemy.goldReward} ALTIN kazandın."
                        )
                    )
                }
            } else {
                val speech = if (newEnemyHp <= battle.enemy.maxHp * 0.35f) {
                    battle.enemy.lowHpSpeech
                } else {
                    battle.enemy.speechLines[(battle.turnNumber - 1) % battle.enemy.speechLines.size]
                }
                beginEnemySpeechThenAttack(
                    afterHit.copy(
                        enemyShakeOffset = 0f,
                        lastDamageText = null,
                        enemySpeechText = speech
                    )
                )
            }
        }
    }

    fun executeAct(act: ActOption) {
        val battle = _uiState.value.battleState ?: return
        if (battle.phase != BattlePhase.SUBMENU_ACT) return
        soundEngine.playMenuSelect()

        val nextMercy = (battle.mercyProgress + act.mercyGain).coerceAtMost(100)
        val updated = battle.copy(
            mercyProgress = nextMercy,
            narratorText = act.narratorResult,
            enemySpeechText = act.enemyReply
        )
        if (nextMercy >= 100) {
            soundEngine.playSpare()
        }
        beginEnemySpeechThenAttack(updated)
    }

    fun useInventoryItem(itemId: String) {
        val state = _uiState.value
        val battle = state.battleState ?: return
        if (battle.phase != BattlePhase.SUBMENU_ITEM) return
        val item = state.inventory.find { it.id == itemId && it.count > 0 } ?: return

        soundEngine.playHeal()
        val healedHp = (state.hp + item.healAmount).coerceAtMost(state.maxHp)
        val updatedInv = state.inventory.map {
            if (it.id == itemId) it.copy(count = it.count - 1) else it
        }
        val speech = battle.enemy.speechLines[(battle.turnNumber - 1) % battle.enemy.speechLines.size]

        _uiState.update {
            it.copy(
                hp = healedHp,
                inventory = updatedInv
            )
        }
        beginEnemySpeechThenAttack(
            battle.copy(
                narratorText = "* ${item.name} yedin! +${healedHp - state.hp} HP yenilendi.",
                enemySpeechText = speech
            )
        )
    }

    fun attemptSpare() {
        val battle = _uiState.value.battleState ?: return
        if (battle.phase != BattlePhase.SUBMENU_MERCY) return

        if (battle.mercyProgress >= 100) {
            soundEngine.playSpare()
            val goldGain = battle.enemy.goldReward
            _uiState.update {
                it.copy(
                    gold = it.gold + goldGain,
                    sparedEnemies = it.sparedEnemies + battle.enemy.id,
                    battleState = battle.copy(
                        phase = BattlePhase.VICTORY_SPARED,
                        narratorText = "* ${battle.enemy.name} BAĞIŞLANDI!\n* 0 XP ve $goldGain ALTIN kazandın."
                    )
                )
            }
        } else {
            soundEngine.playMenuMove()
            beginEnemySpeechThenAttack(
                battle.copy(
                    narratorText = "* Henüz bağışlanmaya hazır değil! EYLEM ile kalbini kazan.",
                    enemySpeechText = "Beni bu kadar\nkolay geçemezsin!"
                )
            )
        }
    }

    fun attemptFlee() {
        val battle = _uiState.value.battleState ?: return
        if (battle.phase != BattlePhase.SUBMENU_MERCY) return
        soundEngine.playMenuMove()
        _uiState.update {
            it.copy(
                battleState = battle.copy(
                    phase = BattlePhase.FLED,
                    narratorText = "* Kaçtın! Ruhun hızlıca uzaklaştı..."
                )
            )
        }
    }

    private fun beginEnemySpeechThenAttack(updatedBattle: BattleState) {
        battleLoopJob?.cancel()
        _uiState.update {
            it.copy(battleState = updatedBattle.copy(phase = BattlePhase.ENEMY_SPEECH))
        }
        battleLoopJob = viewModelScope.launch {
            soundEngine.playBlip(updatedBattle.enemy.voicePitchHz)
            delay(1350L)
            startBulletHellAttack()
        }
    }

    private fun startBulletHellAttack() {
        val battle = _uiState.value.battleState ?: return
        val duration = battle.enemy.attackDurationSec
        _uiState.update {
            it.copy(
                battleState = battle.copy(
                    phase = BattlePhase.ENEMY_ATTACK,
                    soulX = 0.5f,
                    soulY = 0.65f,
                    soulIsMoving = false,
                    invulnerableSec = 0.35f,
                    attackTimeLeftSec = duration,
                    bullets = emptyList()
                )
            )
        }

        battleLoopJob?.cancel()
        battleLoopJob = viewModelScope.launch {
            var elapsed = 0f
            var spawnAccum = 0f
            var waveIdx = 0
            val dt = 0.016f
            val spawnInterval = when (battle.enemy.id) {
                EnemyId.PAPATYA -> 0.72f
                EnemyId.ZIPLAK -> 0.65f
                EnemyId.KEMIK_SOVALYE -> 0.88f
                EnemyId.GOLGE_VEX -> 0.62f
            }

            while (true) {
                val curState = _uiState.value
                val b = curState.battleState ?: break
                if (b.phase != BattlePhase.ENEMY_ATTACK) break

                elapsed += dt
                spawnAccum += dt
                val timeLeft = (duration - elapsed).coerceAtLeast(0f)
                if (timeLeft <= 0f) {
                    val nextTurn = b.turnNumber + 1
                    val flavor = if (b.mercyProgress >= 100) {
                        b.enemy.spareableFlavor
                    } else {
                        b.enemy.ambientFlavors[(nextTurn - 2).mod(b.enemy.ambientFlavors.size)]
                    }
                    _uiState.update {
                        it.copy(
                            battleState = b.copy(
                                phase = BattlePhase.PLAYER_MENU,
                                turnNumber = nextTurn,
                                bullets = emptyList(),
                                narratorText = flavor,
                                enemySpeechText = ""
                            )
                        )
                    }
                    break
                }

                var updatedBullets = b.bullets.mapNotNull { bullet ->
                    val nx = bullet.x + bullet.vx * dt
                    val ny = bullet.y + bullet.vy * dt
                    val nRot = bullet.rotationDeg + bullet.spinDegPerSec * dt
                    if (nx < -0.15f || nx > 1.15f || ny < -0.15f || ny > 1.15f) {
                        null
                    } else {
                        bullet.copy(x = nx, y = ny, rotationDeg = nRot)
                    }
                }

                if (spawnAccum >= spawnInterval && timeLeft > 0.75f) {
                    spawnAccum = 0f
                    updatedBullets = updatedBullets + BulletPatterns.spawnWave(
                        enemyId = b.enemy.id,
                        turnNumber = b.turnNumber,
                        waveIndex = waveIdx++,
                        soulX = b.soulX,
                        soulY = b.soulY
                    )
                }

                var nextInvuln = (b.invulnerableSec - dt).coerceAtLeast(0f)
                var nextHp = curState.hp
                val remainingBullets = mutableListOf<Bullet>()
                val soulRadius = 0.030f

                val isRecentlyMoving = (System.currentTimeMillis() - lastSoulMoveTimeMs) <= 85L

                for (bullet in updatedBullets) {
                    val hit = checkBulletCollision(b.soulX, b.soulY, soulRadius, bullet)
                    if (!hit) {
                        remainingBullets += bullet
                        continue
                    }

                    when (bullet.type) {
                        BulletType.GREEN_HEAL -> {
                            soundEngine.playHeal()
                            nextHp = (nextHp + 3).coerceAtMost(curState.maxHp)
                            // Green bullet gets absorbed
                        }
                        BulletType.CYAN_STILL -> {
                            remainingBullets += bullet
                            if (isRecentlyMoving && nextInvuln <= 0f) {
                                soundEngine.playSoulHurt()
                                nextHp = (nextHp - b.enemy.atk).coerceAtLeast(0)
                                nextInvuln = 0.85f
                            }
                        }
                        BulletType.ORANGE_MOVE -> {
                            remainingBullets += bullet
                            if (!isRecentlyMoving && nextInvuln <= 0f) {
                                soundEngine.playSoulHurt()
                                nextHp = (nextHp - b.enemy.atk).coerceAtLeast(0)
                                nextInvuln = 0.85f
                            }
                        }
                        BulletType.WHITE -> {
                            remainingBullets += bullet
                            if (nextInvuln <= 0f) {
                                soundEngine.playSoulHurt()
                                nextHp = (nextHp - b.enemy.atk).coerceAtLeast(0)
                                nextInvuln = 0.85f
                            }
                        }
                    }
                }

                if (nextHp <= 0) {
                    _uiState.update {
                        it.copy(
                            hp = 0,
                            battleState = b.copy(
                                phase = BattlePhase.GAME_OVER,
                                bullets = emptyList(),
                                narratorText = "* Ruhun parçalandı... Ama KARARLILIĞINI kaybetme!"
                            )
                        )
                    }
                    break
                }

                _uiState.update {
                    it.copy(
                        hp = nextHp,
                        battleState = b.copy(
                            bullets = remainingBullets,
                            attackTimeLeftSec = timeLeft,
                            invulnerableSec = nextInvuln,
                            soulIsMoving = isRecentlyMoving
                        )
                    )
                }
                delay(16L)
            }
        }
    }

    private fun checkBulletCollision(
        soulX: Float,
        soulY: Float,
        soulRadius: Float,
        bullet: Bullet
    ): Boolean {
        return when (bullet.shape) {
            BulletShape.PELLET, BulletShape.STAR -> {
                hypot((soulX - bullet.x).toDouble(), (soulY - bullet.y).toDouble()) <
                    (soulRadius + bullet.radius * 0.82f)
            }
            BulletShape.BONE_V, BulletShape.BONE_H, BulletShape.SPEAR -> {
                val dx = abs(soulX - bullet.x)
                val dy = abs(soulY - bullet.y)
                dx < (bullet.halfWidth + soulRadius * 0.72f) &&
                    dy < (bullet.halfHeight + soulRadius * 0.72f)
            }
        }
    }

    fun moveSoulByNormalizedDelta(dx: Float, dy: Float) {
        val battle = _uiState.value.battleState ?: return
        if (battle.phase != BattlePhase.ENEMY_ATTACK) return
        val movedEnough = hypot(dx.toDouble(), dy.toDouble()) > 0.0022
        if (movedEnough) {
            lastSoulMoveTimeMs = System.currentTimeMillis()
        }
        val nx = (battle.soulX + dx).coerceIn(0.05f, 0.95f)
        val ny = (battle.soulY + dy).coerceIn(0.06f, 0.94f)
        _uiState.update {
            it.copy(
                battleState = battle.copy(
                    soulX = nx,
                    soulY = ny,
                    soulIsMoving = movedEnough || (System.currentTimeMillis() - lastSoulMoveTimeMs) <= 85L
                )
            )
        }
    }

    fun finishBattleVictoryOrFlee() {
        stopAllLoops()
        soundEngine.playMenuSelect()
        val state = _uiState.value
        val battle = state.battleState ?: return

        if (!battle.isFromOverworld) {
            // Direct boss-rush mode returns to Title or Ending if boss defeated
            if (battle.enemy.id == EnemyId.GOLGE_VEX &&
                (battle.phase == BattlePhase.VICTORY_SPARED || battle.phase == BattlePhase.VICTORY_KILLED)
            ) {
                _uiState.update { it.copy(screen = ScreenRoute.ENDING, battleState = null) }
            } else {
                _uiState.update { it.copy(screen = ScreenRoute.TITLE, battleState = null) }
            }
            return
        }

        // Story Overworld mode: if Vex was defeated or spared, proceed to Ending!
        if (battle.enemy.id == EnemyId.GOLGE_VEX &&
            (battle.phase == BattlePhase.VICTORY_SPARED || battle.phase == BattlePhase.VICTORY_KILLED)
        ) {
            _uiState.update { it.copy(screen = ScreenRoute.ENDING, battleState = null) }
        } else {
            _uiState.update { it.copy(screen = ScreenRoute.OVERWORLD, battleState = null) }
        }
    }

    fun retryAfterGameOver() {
        val battle = _uiState.value.battleState ?: return
        soundEngine.playSaveStar()
        _uiState.update { it.copy(hp = it.maxHp) }
        launchBattle(battle.enemy.id, isFromOverworld = battle.isFromOverworld)
    }

    private fun stopAllLoops() {
        typewriterJob?.cancel()
        battleLoopJob?.cancel()
        overworldMoveJob?.cancel()
    }

    override fun onCleared() {
        stopAllLoops()
        super.onCleared()
    }
}
