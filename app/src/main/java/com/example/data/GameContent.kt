package com.example.data

import com.example.model.ActOption
import com.example.model.EnemyDefinition
import com.example.model.EnemyId
import com.example.model.InventoryItem
import com.example.model.OverworldRoom
import com.example.model.RoomDefinition
import com.example.model.TileType

object GameContent {

    val initialInventory: List<InventoryItem> = listOf(
        InventoryItem(
            id = "butterscotch_pie",
            name = "Tarçınlı Turta",
            healAmount = 20,
            description = "Ev yapımı sıcak turta. Tüm canını yeniler!",
            count = 1
        ),
        InventoryItem(
            id = "monster_candy",
            name = "Canavar Şekeri",
            healAmount = 10,
            description = "Meyan kökü aromalı tuhaf şeker. +10 HP",
            count = 2
        ),
        InventoryItem(
            id = "snowman_piece",
            name = "Kardan Parçası",
            healAmount = 15,
            description = "Dünyayı gezmek isteyen kar tanesi. +15 HP",
            count = 1
        ),
        InventoryItem(
            id = "borek",
            name = "Sıcak Börek",
            healAmount = 12,
            description = "Çıtır çıtır yeraltı böreği. +12 HP",
            count = 1
        )
    )

    val enemies: Map<EnemyId, EnemyDefinition> = mapOf(
        EnemyId.PAPATYA to EnemyDefinition(
            id = EnemyId.PAPATYA,
            name = "Papatya Çiçekcik",
            subtitle = "Harabelerin Kurnaz Çiçeği",
            maxHp = 50,
            atk = 4,
            def = 1,
            xpReward = 15,
            goldReward = 20,
            voicePitchHz = 620f,
            encounterFlavor = "* Küçük bir çiçek gülümseyerek yolunu kesti!",
            ambientFlavors = listOf(
                "* Papatya yapraklarını şüpheli şekilde kıpırdatıyor.",
                "* Topraktan hafif bir kıkırdama yükseliyor.",
                "* Papatya 'dostluk tohumlarını' hazırlıyor."
            ),
            spareableFlavor = "* Papatya şaşkınlıktan kızardı! Artık BAĞIŞLANABİLİR.",
            speechLines = listOf(
                "Merhaba! Ben Papatya!\nBurada işler\nKARARLILIK ile yürür!",
                "Hadi ama, bu minik\nbeyaz yaprakları\nyakalamayı dene!",
                "Haha! Kaçmakta\nbayağı iyisin ha?"
            ),
            lowHpSpeech = "Dur... Yapraklarım\ndökülüyor! Şaka\nyapıyordum sadece!",
            acts = listOf(
                ActOption(
                    id = "check",
                    label = "İncele",
                    narratorResult = "* PAPATYA - ATK 4 DEF 1.\n* Tatlı görünür ama şakası acıtır.\n* İltifat veya sulama işe yarayabilir.",
                    enemyReply = "Beni incelemeyi\nbırak da oyuna bak!",
                    mercyGain = 15
                ),
                ActOption(
                    id = "compliment",
                    label = "Övgü Et",
                    narratorResult = "* Papatya'ya altın gibi parladığını söyledin.",
                    enemyReply = "N-ne?! Tabii ki\nparlıyorum! Ama...\nteşekkürler sanırım.",
                    mercyGain = 50
                ),
                ActOption(
                    id = "water",
                    label = "Su Ver",
                    narratorResult = "* Matarandan Papatya'nın köküne serin su döktün.",
                    enemyReply = "Ohh... Bu çok ferah!\nBelki de o kadar\nkötü değilsindir.",
                    mercyGain = 55
                )
            ),
            attackDurationSec = 5.5f
        ),

        EnemyId.ZIPLAK to EnemyDefinition(
            id = EnemyId.ZIPLAK,
            name = "Zıplak Kurbağa",
            subtitle = "Yosunlu Koridorun Utangaç Sakinleri",
            maxHp = 65,
            atk = 5,
            def = 2,
            xpReward = 20,
            goldReward = 25,
            voicePitchHz = 290f,
            encounterFlavor = "* Zıplak çekingen gözlerle sana yaklaştı!",
            ambientFlavors = listOf(
                "* Zıplak etraftaki sineklere göz kırpıyor.",
                "* Vırak! Zıplak ne diyeceğini bilemiyor.",
                "* Koridorda nemli bir vıraklama yankılandı."
            ),
            spareableFlavor = "* Zıplak mutlu mutlu vıraklıyor! Artık BAĞIŞLANABİLİR.",
            speechLines = listOf(
                "Vırak... Merhaba\nyabancı. (Kızarır)",
                "Zıplamak benim\ntek yeteneğim... vırak!",
                "Sineklerim seni\nbiraz gıdıklayabilir."
            ),
            lowHpSpeech = "Vı-vırak... Çok sert\nvuruyorsun...",
            acts = listOf(
                ActOption(
                    id = "check",
                    label = "İncele",
                    narratorResult = "* ZIPLAK - ATK 5 DEF 2.\n* Kurbağa kurallarına pek uymuyor.\n* İltifatları çok sever!",
                    enemyReply = "Vırak? Benimle\nilgileniyor musun?",
                    mercyGain = 20
                ),
                ActOption(
                    id = "compliment",
                    label = "İltifat Et",
                    narratorResult = "* Zıplak'a harika zıpladığını söyledin. Anlamasa da gururlandı!",
                    enemyReply = "Vırak!! (Mutluluktan\nyerinde zıplıyor!)",
                    mercyGain = 50
                ),
                ActOption(
                    id = "flirt",
                    label = "Göz Kırp",
                    narratorResult = "* Zıplak'a dostça göz kırptın. Utandı ama çok hoşuna gitti!",
                    enemyReply = "Vı-vırak?! Böyle\ngüzel sözlere alışık\ndeğilim!",
                    mercyGain = 55
                ),
                ActOption(
                    id = "croak",
                    label = "Vırakla",
                    narratorResult = "* Zıplak ile aynı tonda vırakladın. Kurbağa dilinde anlaşma sağlandı!",
                    enemyReply = "Vırak vırak! (Artık\nen iyi arkadaşısın!)",
                    mercyGain = 60
                )
            ),
            attackDurationSec = 6.0f
        ),

        EnemyId.KEMIK_SOVALYE to EnemyDefinition(
            id = EnemyId.KEMIK_SOVALYE,
            name = "Şövalye Kemik",
            subtitle = "Karlı Nöbetin Büyük Şefi",
            maxHp = 110,
            atk = 6,
            def = 4,
            xpReward = 45,
            goldReward = 50,
            voicePitchHz = 490f,
            encounterFlavor = "* ŞÖVALYE KEMİK tüm ihtişamıyla yolu kapattı! NYEH HEH!",
            ambientFlavors = listOf(
                "* Kemik atkısını kahramanca savuruyor.",
                "* Havada taze pişmiş spagetti kokusu var.",
                "* Kemik özel MAVİ kemik saldırısını hazırlıyor!"
            ),
            spareableFlavor = "* Şövalye Kemik seni arkadaşı ilan etti! BAĞIŞLANABİLİR.",
            speechLines = listOf(
                "NYEH HEH HEH!\nMAVİ kemiklerde\nHAREKETSİZ KAL!",
                "TURUNCU kemiklerde\nise DURMA, KOŞ!\nHarika değil mi?!",
                "Benim spagetti\ntarifime kimse\ndayanamaz!"
            ),
            lowHpSpeech = "Vay canına... Kemiklerim\nsızlıyor ama yine de\narkadaş olabiliriz!",
            acts = listOf(
                ActOption(
                    id = "check",
                    label = "İncele",
                    narratorResult = "* ŞÖVALYE KEMİK - ATK 6 DEF 4.\n* Kalbi altından bir iskelet.\n* Mavi kemikte DUR, Turuncu kemikte HAREKET ET!",
                    enemyReply = "Beni çözdün mü?!\nHarika bir rakipsin!",
                    mercyGain = 20
                ),
                ActOption(
                    id = "spaghetti",
                    label = "Spagetti Öv",
                    narratorResult = "* Şövalye Kemik'in meşhur spagettisinin kokusunu övdün!",
                    enemyReply = "GERÇEKTEN Mİ?!\nBir gün sana bir\ntabak yapacağım!",
                    mercyGain = 45
                ),
                ActOption(
                    id = "joke",
                    label = "Kemik Şakası",
                    narratorResult = "* 'Bu savaş gerçekten kemik kıran cinsten!' dedin.",
                    enemyReply = "OHH HAYIR, KARDEŞİM\nGİBİ KONUŞUYORSUN!\n(Ama güldü!)",
                    mercyGain = 45
                ),
                ActOption(
                    id = "befriend",
                    label = "Dostluk Teklif Et",
                    narratorResult = "* Savaşmak yerine iyi bir arkadaş olabileceğinizi söyledin.",
                    enemyReply = "B-BENİMLE ARKADAŞ\nMI OLACAKSIN?!\nNYEH HEH... EVET!",
                    mercyGain = 60
                )
            ),
            attackDurationSec = 7.0f
        ),

        EnemyId.GOLGE_VEX to EnemyDefinition(
            id = EnemyId.GOLGE_VEX,
            name = "Gölge Şövalyesi Vex",
            subtitle = "Çekirdek Tahtının Son Muhafızı",
            maxHp = 160,
            atk = 7,
            def = 6,
            xpReward = 90,
            goldReward = 100,
            voicePitchHz = 210f,
            encounterFlavor = "* Gölge Şövalyesi Vex mızrağını yere vurdu! Hava titriyor!",
            ambientFlavors = listOf(
                "* Taht salonunun gölgeleri etrafında dönüyor.",
                "* Vex'in gözleri kararlılığını sınıyor.",
                "* Mavi ve Turuncu enerjiler havada çarpışıyor!"
            ),
            spareableFlavor = "* Vex mızrağını indirdi ve eğildi. Artık BAĞIŞLANABİLİR!",
            speechLines = listOf(
                "Buraya kadar geldin,\nküçük ruh...\nKararlılığını göster!",
                "Mavi dalgada DUR!\nTuruncu dalgada HAREKET ET!\nYeşil özü topla!",
                "Sadece güç değil...\nMerhamet de bir\nyürek işidir."
            ),
            lowHpSpeech = "Demek gücün bu kadar\nbüyük... Son hamleni\nyap o zaman.",
            acts = listOf(
                ActOption(
                    id = "check",
                    label = "İncele",
                    narratorResult = "* GÖLGE ŞÖVALYESİ VEX - ATK 7 DEF 6.\n* Yeraltı'nın onurlu koruyucusu.\n* Onu anlamaya ve ikna etmeye çalış.",
                    enemyReply = "Ruhumun derinliklerini\nmı okuyorsun?",
                    mercyGain = 15
                ),
                ActOption(
                    id = "listen",
                    label = "Dinle",
                    narratorResult = "* Vex'in neden bu tahtı koruduğunu sessizce dinledin.",
                    enemyReply = "Bunca yıldır kimse\nbizi dinlemedi...\nGözlerin samimi.",
                    mercyGain = 35
                ),
                ActOption(
                    id = "hope",
                    label = "Umut Ver",
                    narratorResult = "* Yüzeye çıkan kapının barışla açılacağına söz verdin.",
                    enemyReply = "Barış mı...? Belki de\nyeraltının güneşi\nsenin elindedir.",
                    mercyGain = 40
                ),
                ActOption(
                    id = "bow",
                    label = "Saygıyla Eğil",
                    narratorResult = "* Kılıcını değil, yüreğini göstererek şövalyenin önünde eğildin.",
                    enemyReply = "Bir şövalyeye yakışır\nonur... Mızrağımı\nindiriyorum.",
                    mercyGain = 45
                )
            ),
            attackDurationSec = 7.5f
        )
    )

    // 12x10 Tilemaps:
    // '#' = WALL, '.' = FLOOR, '=' = PATH, '*' = SAVE_STAR, '?' = SIGN
    // 'S' = SWITCH_PLATE, 'X' = SPIKES (open if switchPressed), 'C' = CANDY_BOWL
    // '>' = PORTAL_NEXT, '<' = PORTAL_PREV
    // '1' = NPC_PAPATYA, '2' = NPC_ZIPLAK, '3' = NPC_KEMIK, '4' = NPC_VEX
    val rooms: Map<OverworldRoom, RoomDefinition> = mapOf(
        OverworldRoom.RUINS_ENTRANCE to RoomDefinition(
            room = OverworldRoom.RUINS_ENTRANCE,
            grid = listOf(
                "############",
                "#....==....#",
                "#..*.==..?.#",
                "#....==....#",
                "#....1.....#",
                "#....==....#",
                "#....==....#",
                "#....==....#",
                "#..........#",
                "#####>>#####"
            ),
            spawnFromPrev = 5.5f to 2.5f,
            spawnFromNext = 5.5f to 7.5f
        ),
        OverworldRoom.MOSSY_CORRIDOR to RoomDefinition(
            room = OverworldRoom.MOSSY_CORRIDOR,
            grid = listOf(
                "#####<<#####",
                "#....==....#",
                "#.C..==..S.#",
                "#....==....#",
                "####XXXX####",
                "#....==....#",
                "#..?.2.....#",
                "#....==....#",
                "#....==....#",
                "#####>>#####"
            ),
            spawnFromPrev = 5.5f to 1.5f,
            spawnFromNext = 5.5f to 8.2f
        ),
        OverworldRoom.SNOWY_OUTPOST to RoomDefinition(
            room = OverworldRoom.SNOWY_OUTPOST,
            grid = listOf(
                "#####<<#####",
                "#....==....#",
                "#.*..==..?.#",
                "#....==....#",
                "#....3.....#",
                "#....==....#",
                "#..======..#",
                "#....==....#",
                "#..........#",
                "#####>>#####"
            ),
            spawnFromPrev = 5.5f to 1.5f,
            spawnFromNext = 5.5f to 8.2f
        ),
        OverworldRoom.THRONE_HALL to RoomDefinition(
            room = OverworldRoom.THRONE_HALL,
            grid = listOf(
                "#####<<#####",
                "#....==....#",
                "#..*.==..?.#",
                "#....==....#",
                "#....==....#",
                "#....4.....#",
                "#....==....#",
                "#..........#",
                "#..........#",
                "############"
            ),
            spawnFromPrev = 5.5f to 1.5f,
            spawnFromNext = 5.5f to 7.0f
        )
    )

    fun parseTile(char: Char, switchPressed: Boolean): TileType = when (char) {
        '#' -> TileType.WALL
        '=' -> TileType.PATH
        '*' -> TileType.SAVE_STAR
        '?' -> TileType.SIGN
        'S' -> TileType.SWITCH_PLATE
        'X' -> if (switchPressed) TileType.SPIKES_OPEN else TileType.SPIKES_CLOSED
        'C' -> TileType.CANDY_BOWL
        '>' -> TileType.PORTAL_NEXT
        '<' -> TileType.PORTAL_PREV
        '1' -> TileType.NPC_PAPATYA
        '2' -> TileType.NPC_ZIPLAK
        '3' -> TileType.NPC_KEMIK
        '4' -> TileType.NPC_VEX
        else -> TileType.FLOOR
    }
}
