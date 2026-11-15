package com.example.model

enum class ScreenRoute {
    TITLE,
    OVERWORLD,
    BATTLE,
    ENDING
}

enum class EnemyId {
    PAPATYA,
    ZIPLAK,
    KEMIK_SOVALYE,
    GOLGE_VEX
}

enum class Direction {
    DOWN, UP, LEFT, RIGHT
}

enum class OverworldRoom(val title: String) {
    RUINS_ENTRANCE("Harabe Girişi"),
    MOSSY_CORRIDOR("Yosunlu Koridor"),
    SNOWY_OUTPOST("Karlı Nöbet Yeri"),
    THRONE_HALL("Çekirdek Taht Salonu")
}

enum class TileType {
    FLOOR,
    WALL,
    PATH,
    SAVE_STAR,
    SPIKES_CLOSED,
    SPIKES_OPEN,
    SWITCH_PLATE,
    PORTAL_NEXT,
    PORTAL_PREV,
    SIGN,
    CANDY_BOWL,
    NPC_PAPATYA,
    NPC_ZIPLAK,
    NPC_KEMIK,
    NPC_VEX
}

data class RoomDefinition(
    val room: OverworldRoom,
    val grid: List<String>,
    val spawnFromPrev: Pair<Float, Float>,
    val spawnFromNext: Pair<Float, Float>
)

data class OverworldDialog(
    val speaker: String,
    val lines: List<String>,
    val currentLineIndex: Int = 0,
    val triggerBattle: EnemyId? = null,
    val voicePitchHz: Float = 440f,
    val healsPlayer: Boolean = false
)

enum class BattleAction {
    FIGHT, ACT, ITEM, MERCY
}

enum class BattlePhase {
    PLAYER_MENU,
    SUBMENU_ACT,
    SUBMENU_ITEM,
    SUBMENU_MERCY,
    FIGHT_TIMING,
    PLAYER_SLASH_EFFECT,
    ENEMY_SPEECH,
    ENEMY_ATTACK,
    VICTORY_SPARED,
    VICTORY_KILLED,
    FLED,
    GAME_OVER
}

enum class BulletType {
    WHITE,
    CYAN_STILL,
    ORANGE_MOVE,
    GREEN_HEAL
}

enum class BulletShape {
    PELLET,
    BONE_V,
    BONE_H,
    SPEAR,
    STAR
}

data class Bullet(
    val id: Long,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val radius: Float = 0.032f,
    val halfWidth: Float = 0.025f,
    val halfHeight: Float = 0.025f,
    val type: BulletType = BulletType.WHITE,
    val shape: BulletShape = BulletShape.PELLET,
    val rotationDeg: Float = 0f,
    val spinDegPerSec: Float = 0f
)

data class ActOption(
    val id: String,
    val label: String,
    val narratorResult: String,
    val enemyReply: String,
    val mercyGain: Int
)

data class EnemyDefinition(
    val id: EnemyId,
    val name: String,
    val subtitle: String,
    val maxHp: Int,
    val atk: Int,
    val def: Int,
    val xpReward: Int,
    val goldReward: Int,
    val voicePitchHz: Float,
    val encounterFlavor: String,
    val ambientFlavors: List<String>,
    val spareableFlavor: String,
    val speechLines: List<String>,
    val lowHpSpeech: String,
    val acts: List<ActOption>,
    val attackDurationSec: Float
)

data class InventoryItem(
    val id: String,
    val name: String,
    val healAmount: Int,
    val description: String,
    val count: Int
)

data class BattleState(
    val enemy: EnemyDefinition,
    val enemyHp: Int,
    val mercyProgress: Int = 0, // 0..100, at 100 enemy name turns yellow (spareable!)
    val turnNumber: Int = 1,
    val selectedAction: BattleAction = BattleAction.FIGHT,
    val phase: BattlePhase = BattlePhase.PLAYER_MENU,
    val narratorText: String,
    val enemySpeechText: String = "",
    val fightCursorPos: Float = 0f, // 0f..1f
    val fightCursorDirection: Float = 1f,
    val lastDamageText: String? = null,
    val slashProgress: Float = 0f,
    val enemyShakeOffset: Float = 0f,
    val soulX: Float = 0.5f,
    val soulY: Float = 0.5f,
    val soulIsMoving: Boolean = false,
    val invulnerableSec: Float = 0f,
    val attackTimeLeftSec: Float = 5f,
    val bullets: List<Bullet> = emptyList(),
    val isFromOverworld: Boolean = true
)

enum class EndingRoute {
    TRUE_PACIFIST,
    NEUTRAL,
    NO_MERCY
}

data class GameUiState(
    val screen: ScreenRoute = ScreenRoute.TITLE,
    val playerName: String = "FRİSK",
    val hp: Int = 20,
    val maxHp: Int = 20,
    val lv: Int = 1,
    val xp: Int = 0,
    val gold: Int = 15,
    val inventory: List<InventoryItem> = emptyList(),
    val currentRoom: OverworldRoom = OverworldRoom.RUINS_ENTRANCE,
    val playerX: Float = 5.5f,
    val playerY: Float = 7.5f,
    val playerDirection: Direction = Direction.UP,
    val isMovingOverworld: Boolean = false,
    val switchPressed: Boolean = false,
    val candyTakenCount: Int = 0,
    val sparedEnemies: Set<EnemyId> = emptySet(),
    val killedEnemies: Set<EnemyId> = emptySet(),
    val overworldDialog: OverworldDialog? = null,
    val visibleDialogChars: Int = 0,
    val battleState: BattleState? = null,
    val isMuted: Boolean = false,
    val statusToast: String? = null
) {
    val resolvedEnemies: Set<EnemyId> get() = sparedEnemies + killedEnemies
    val endingRoute: EndingRoute
        get() = when {
            killedEnemies.isEmpty() && sparedEnemies.isNotEmpty() -> EndingRoute.TRUE_PACIFIST
            sparedEnemies.isEmpty() && killedEnemies.isNotEmpty() -> EndingRoute.NO_MERCY
            else -> EndingRoute.NEUTRAL
        }
}
