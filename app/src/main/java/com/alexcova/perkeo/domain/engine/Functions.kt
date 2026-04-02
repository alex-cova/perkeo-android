package com.alexcova.perkeo.domain.engine

import com.alexcova.perkeo.domain.util.LuaRandom

class Cache {
    val nodes = mutableMapOf<String, Double>()
    var generatedFirstPack = false
}

class Functions(val seed: String, val ante: Int) : LockState() {
    val params: InstanceParams = InstanceParams()
    val cache: Cache = Cache()
    val hashedSeed: Double = Util.pseudohash(seed)

    fun setParams(p: InstanceParams) { params.deck = p.deck; params.stake = p.stake; params.showman = p.showman; params.sixesFactor = p.sixesFactor }

    fun randint(id: String, min: Int, max: Int) = LuaRandom.randomInt(getNode(id), min, max)
    fun random(id: String) = LuaRandom.random(getNode(id))

    fun getNode(id: String): Double {
        var c = cache.nodes[id]
        if (c == null) { c = Util.pseudohash("$id$seed"); cache.nodes[id] = c }
        val pre = (c * 1.72431234 + 2.134453429141) % 1.0
        val value = Util.round13(pre)
        cache.nodes[id] = value
        return (value + hashedSeed) / 2.0
    }

    fun randweightedchoice(id: String, items: List<PackType>): PackType {
        var poll = random(id) * 22.42
        var idx = 1
        var weight = 0.0
        while (weight < poll) { weight += items[idx].value; idx++ }
        return items[idx - 1]
    }

    fun <T : Item> randchoice(id: String, items: List<T>): T {
        val item = items[randint(id, 0, items.size - 1)]
        if (item.isRetry()) return resample(id, items)
        if (!params.showman && isLocked(item)) return resample(id, items)
        return item
    }

    fun <T : Item> resample(id: String, items: List<T>): T {
        var resample = 2
        while (true) {
            val item = items[randint("${id}_resample$resample", 0, items.size - 1)]
            resample++
            if (!item.isRetry() && !isLocked(item) || resample > 1000) return item
        }
    }

    fun nextTarot(source: String, ante: Int, soulable: Boolean): Item {
        if (soulable && (params.showman || !isLocked(Specials.THE_SOUL)) && random(soul_TarotArr[ante]) > 0.997) {
            val data = nextJoker("sou", joker1SouArr, joker2SouArr, joker3SouArr, joker4SouArr, raritySouArr, editionSouArr, ante, true)
            lock(data.joker); lock(Specials.THE_SOUL)
            return EditionItem(data.edition, data.joker)
        }
        val tarot = randchoice(source, TAROTS)
        return if (tarot == Tarot.The_Wheel_of_Fortune) EditionItem(nextWheelOfFortune(), tarot) else tarot
    }

    fun nextPlanet(source: String, ante: Int, soulable: Boolean): Item {
        if (soulable && (params.showman || !isLocked(Specials.BLACKHOLE)) && random(soul_PlanetArr[ante]) > 0.997)
            return Specials.BLACKHOLE
        return randchoice(source, PLANETS)
    }

    fun nextSpectral(source: String, ante: Int, soulable: Boolean): Item {
        if (soulable) {
            var forcedKey: Item? = null; var edition = Edition.NoEdition
            if ((params.showman || !isLocked(Specials.THE_SOUL)) && random(soul_SpectralArr[ante]) > 0.997) {
                val data = nextJoker("sou", joker1SouArr, joker2SouArr, joker3SouArr, joker4SouArr, raritySouArr, editionSouArr, ante, true)
                forcedKey = data.joker; edition = data.edition; lock(data.joker)
            }
            if ((params.showman || !isLocked(Specials.BLACKHOLE)) && random(soul_SpectralArr[ante]) > 0.997)
                forcedKey = Specials.BLACKHOLE
            if (forcedKey != null) return EditionItem(edition, forcedKey)
        }
        return randchoice(source, SPECTRALS)
    }

    fun nextWheelOfFortune(): Edition {
        if (random("wheel_of_fortune") > 0.25) return pollEdition("wheel_of_fortune", null, noNegative = true, guaranteed = true)
        return Edition.NoEdition
    }

    fun edition(ante: Int, editionArr: List<String>): Edition {
        val editionRate = getEditionRate(); val poll = random(editionArr[ante])
        return when {
            poll > 0.997 -> Edition.Negative
            poll > (1 - 0.006 * editionRate) -> Edition.Polychrome
            poll > (1 - 0.02 * editionRate) -> Edition.Holographic
            poll > (1 - 0.04 * editionRate) -> Edition.Foil
            else -> Edition.NoEdition
        }
    }

    fun getEditionRate() = when {
        isVoucherActive(Voucher.Glow_Up) -> 4.0
        isVoucherActive(Voucher.Hone) -> 2.0
        else -> 1.0
    }

    fun pollEdition(coord: String, modifier: Double?, noNegative: Boolean, guaranteed: Boolean): Edition {
        val poll = random(coord); val editionRate = getEditionRate()
        fun multi(rate: Double) = if (modifier == null) rate else rate * modifier
        return if (guaranteed) when {
            poll > 1 - 0.003 * 25 && !noNegative -> Edition.Polychrome
            poll > 1 - 0.006 * 25 -> Edition.Polychrome
            poll > 1 - 0.02 * 25 -> Edition.Holographic
            poll > 1 - 0.04 * 25 -> Edition.Foil
            else -> Edition.NoEdition
        } else when {
            poll > 1 - multi(0.003) && !noNegative -> Edition.Negative
            poll > 1 - 0.006 * multi(editionRate) -> Edition.Polychrome
            poll > 1 - 0.02 * multi(editionRate) -> Edition.Holographic
            poll > 1 - 0.04 * multi(editionRate) -> Edition.Foil
            else -> Edition.NoEdition
        }
    }

    val setA = setOf("Gros Michel","Ice Cream","Cavendish","Luchador","Turtle Bean","Diet Cola","Popcorn","Ramen","Seltzer","Mr. Bones","Invisible Joker")
    val setB = setOf("Ceremonial Dagger","Ride the Bus","Runner","Constellation","Green Joker","Red Card","Madness","Square Joker","Vampire","Rocket","Obelisk","Lucky Cat","Flash Card","Spare Trousers","Castle","Wee Joker")

    fun nextJoker(source: String, joker1Arr: List<String>, joker2Arr: List<String>, joker3Arr: List<String>,
                  joker4Arr: List<String>, rarityArr: List<String>, editionArr: List<String>, ante: Int, hasStickers: Boolean): JokerData {
        val rarity = when (source) {
            "sou" -> "4"; "wra", "rta" -> "3"; "uta" -> "2"
            else -> {
                val p = random(rarityArr[ante])
                when { p > 0.95 -> "3"; p > 0.7 -> "2"; else -> "1" }
            }
        }
        val ed = edition(ante, editionArr)
        val joker: Item = when (rarity) {
            "4" -> randchoice("Joker4", LEGENDARY_JOKERS)
            "3" -> randchoice(joker3Arr[ante], RARE_JOKERS)
            "2" -> randchoice(joker2Arr[ante], UNCOMMON_JOKERS)
            else -> randchoice(joker1Arr[ante], COMMON_JOKERS)
        }
        val stickers = JokerStickers()
        if (hasStickers) {
            val searchForSticker = params.stake in listOf(Stake.Black_Stake, Stake.Blue_Stake, Stake.Purple_Stake, Stake.Orange_Stake, Stake.Gold_Stake)
            var stickerPoll = 0.0
            if (searchForSticker) stickerPoll = random(if (source == "buf") packetperArr[ante] else etperpollArr[ante])
            if (stickerPoll > 0.7 && joker.rawValue !in setA) stickers.eternal = true
            if (stickerPoll > 0.4 && stickerPoll <= 0.7 && params.stake in listOf(Stake.Orange_Stake, Stake.Gold_Stake) && joker.rawValue !in setB)
                stickers.perishable = true
            if (params.stake == Stake.Gold_Stake)
                stickers.rental = random(if (source == "buf") packssjrArr[ante] else ssjrArr[ante]) > 0.7
        }
        val type = when (joker) { is LegendaryJoker -> JokerType.LEGENDARY; is RareJoker -> JokerType.RARE; is UnCommonJoker -> JokerType.UNCOMMON; else -> JokerType.COMMON }
        return JokerData(joker, type, ed, stickers)
    }

    fun getShopInstance(): ShopInstance {
        var tarotRate = 4.0; var planetRate = 4.0; var playingCardRate = 0.0; var spectralRate = 0.0
        if (params.deck == Deck.GHOST_DECK) spectralRate = 2.0
        if (isVoucherActive(Voucher.Tarot_Tycoon)) tarotRate = 32.0 else if (isVoucherActive(Voucher.Tarot_Merchant)) tarotRate = 9.6
        if (isVoucherActive(Voucher.Planet_Tycoon)) planetRate = 32.0 else if (isVoucherActive(Voucher.Planet_Merchant)) planetRate = 9.6
        if (isVoucherActive(Voucher.Magic_Trick)) playingCardRate = 4.0
        return ShopInstance(20.0, tarotRate, planetRate, playingCardRate, spectralRate)
    }

    fun nextShopItem(ante: Int): ShopItem {
        val shop = getShopInstance(); var cdtPoll = random(cdtArr[ante]) * shop.getTotalRate()
        val type = when {
            cdtPoll < shop.jokerRate -> ItemType.Joker
            run { cdtPoll -= shop.jokerRate; cdtPoll } < shop.tarotRate -> ItemType.Tarot
            run { cdtPoll -= shop.tarotRate; cdtPoll } < shop.planetRate -> ItemType.Planet
            run { cdtPoll -= shop.planetRate; cdtPoll } < shop.playingCardRate -> ItemType.PlayingCard
            else -> ItemType.Spectral
        }
        return when (type) {
            ItemType.Joker -> { val jkr = nextJoker("sho", joker1ShoArr, joker2ShoArr, joker3ShoArr, joker4ShoArr, rarityShoArr, editionShoArr, ante, true); ShopItem(type, jkr.joker, jkr) }
            ItemType.Tarot -> ShopItem(type, nextTarot(tarotShoArr[ante], ante, false))
            ItemType.Planet -> ShopItem(type, nextPlanet(planetShoArr[ante], ante, false))
            ItemType.Spectral -> ShopItem(type, nextSpectral(spectralShoArr[ante], ante, false))
            ItemType.PlayingCard -> ShopItem(type, nextStandardCard(ante))
        }
    }

    fun nextPack(ante: Int): PackType {
        if (ante <= 2 && !cache.generatedFirstPack) { cache.generatedFirstPack = true; return PackType.Buffoon_Pack }
        return randweightedchoice(shop_packArr[ante], PACKS)
    }

    fun packInfo(pack: PackType): Pack {
        val size = if (pack.isBuffoon || pack.isSpectral) { if (pack.isMega) 4 else if (pack.isJumbo) 4 else 2 } else { if (pack.isMega) 5 else if (pack.isJumbo) 5 else 3 }
        val choices = if (pack.isMega) 2 else 1
        return Pack(pack, size, choices)
    }

    fun nextStandardCard(ante: Int): Card {
        val enhancement = if (random(stdsetArr[ante]) <= 0.6) null else randchoice(enhancedstaArr[ante], ENHANCEMENTS)
        val base = randchoice(frontstaArr[ante], CARDS)
        val editionPoll = random(standard_editionArr[ante])
        val edition = when { editionPoll > 0.988 -> Edition.Polychrome; editionPoll > 0.96 -> Edition.Holographic; editionPoll > 0.92 -> Edition.Foil; else -> Edition.NoEdition }
        val sealPoll = random(stdsealtypeArr[ante])
        val seal = if (random(stdsealArr[ante]) <= 0.8) Seal.NoSeal else when { sealPoll > 0.75 -> Seal.RedSeal; sealPoll > 0.5 -> Seal.BlueSeal; sealPoll > 0.25 -> Seal.GoldSeal; else -> Seal.PurpleSeal }
        return Card(base, enhancement, edition, seal)
    }

    fun nextArcanaPack(size: Int, ante: Int): List<Item> {
        val pack = mutableListOf<Item>()
        for (i in 0 until size) {
            val item = if (isVoucherActive(Voucher.Omen_Globe) && random("omen_globe") > 0.8) nextSpectral(spectralAr2Arr[ante], ante, true)
                       else nextTarot(tarotAr1Arr[ante], ante, true)
            pack.add(item)
            if (!params.showman) lock(pack[i])
        }
        if (params.showman) return pack
        for (p in pack) { if (p is EditionItem) unlock(Specials.THE_SOUL) else unlock(p) }
        return pack
    }

    fun nextCelestialPack(size: Int, ante: Int): List<Item> {
        val pack = mutableListOf<Item>()
        for (i in 0 until size) { pack.add(nextPlanet(planetpl1lArr[ante], ante, true)); if (!params.showman) lock(pack[i]) }
        if (params.showman) return pack
        for (p in pack) unlock(p); return pack
    }

    fun nextSpectralPack(size: Int, ante: Int): List<Item> {
        val pack = mutableListOf<Item>()
        for (i in 0 until size) { pack.add(nextSpectral(spectralSpeArr[ante], ante, true)); if (!params.showman) lock(pack[i]) }
        if (params.showman) return pack
        for (p in pack) { if (p !is EditionItem) unlock(p) }; return pack
    }

    fun nextStandardPack(size: Int, ante: Int) = (0 until size).map { nextStandardCard(ante) }

    fun nextBuffoonPack(size: Int, ante: Int): List<JokerData> {
        val pack = mutableListOf<JokerData>()
        for (i in 0 until size) {
            pack.add(nextJoker("buf", joker1BufArr, joker2BufArr, joker3BufArr, joker4BufArr, rarityBufArr, editionBufArr, ante, true))
            if (!params.showman) lock(pack[i].joker)
        }
        if (params.showman) return pack
        for (p in pack) unlock(p.joker); return pack
    }

    fun isVoucherActive(voucher: Voucher) = params.vouchers.contains(voucher)

    fun activateVoucher(voucher: Voucher) {
        params.vouchers.add(voucher); lock(voucher)
        for (i in 0 until VOUCHERS.size step 2) {
            if (VOUCHERS[i] == voucher) unlock(VOUCHERS[i + 1])
        }
    }

    fun nextVoucher(ante: Int): Voucher = randchoice(VoucherArr[ante], VOUCHERS)

    fun setDeck(deck: Deck) {
        params.deck = deck
        when (deck) { Deck.MAGIC_DECK -> activateVoucher(Voucher.Crystal_Ball); Deck.NEBULA_DECK -> activateVoucher(Voucher.Telescope)
            Deck.ZODIAC_DECK -> { activateVoucher(Voucher.Tarot_Merchant); activateVoucher(Voucher.Planet_Merchant); activateVoucher(Voucher.Overstock) }
            else -> {} }
    }

    fun nextTag(ante: Int): Tag = randchoice(TagArr[ante], TAGS)

    fun nextBoss(ante: Int): Boss {
        var bossPool = Boss.allCases().filter { !isLocked(it) && if (ante % 8 == 0) !it.startsWithT else it.startsWithT }
        if (bossPool.isEmpty()) {
            Boss.allCases().filter { if (ante % 8 == 0) !it.startsWithT else it.startsWithT }.forEach { unlock(it) }
            return nextBoss(ante)
        }
        val chosen = randchoice("boss", bossPool); lock(chosen); return chosen
    }

    companion object {
        val TAROTS = Tarot.allCases(); val PLANETS = Planet.allCases(); val SPECTRALS = Spectral.entries.toList()
        val LEGENDARY_JOKERS = LegendaryJoker.allCases(); val UNCOMMON_JOKERS = UnCommonJoker.allCases()
        val CARDS = Cards.allCases(); val ENHANCEMENTS = Enhancement.allCases(); val VOUCHERS = Voucher.allCases()
        val TAGS = Tag.allCases(); val PACKS = PackType.entries.toList(); val RARE_JOKERS = RareJoker.allCases()
        val COMMON_JOKERS = CommonJoker.allCases(); val BOSSES = Boss.allCases()

        private fun construct(pattern: String) = (0..30).map { String.format(pattern, it) }
        val planetShoArr = construct("Planetsho%d"); val planetpl1lArr = construct("Planetpl1%d")
        val tarotShoArr = construct("Tarotsho%d"); val tarotAr1Arr = construct("Tarotar1%d")
        val spectralShoArr = construct("Spectralsho%d"); val spectralAr2Arr = construct("Spectralar2%d"); val spectralSpeArr = construct("Spectralspe%d")
        val joker4ShoArr = construct("Joker4sho%d"); val joker4BufArr = construct("Joker4buf%d"); val joker4SouArr = construct("Joker4sou%d")
        val joker3ShoArr = construct("Joker3sho%d"); val joker3BufArr = construct("Joker3buf%d"); val joker3SouArr = construct("Joker3sou%d")
        val joker2ShoArr = construct("Joker2sho%d"); val joker2BufArr = construct("Joker2buf%d"); val joker2SouArr = construct("Joker2sou%d")
        val joker1ShoArr = construct("Joker1sho%d"); val joker1BufArr = construct("Joker1buf%d"); val joker1SouArr = construct("Joker1sou%d")
        val rarityShoArr = construct("rarity%dsho"); val rarityBufArr = construct("rarity%dbuf"); val raritySouArr = construct("rarity%dsou")
        val editionShoArr = construct("edisho%d"); val editionBufArr = construct("edibuf%d"); val editionSouArr = construct("edisou%d")
        val packssjrArr = construct("packssjr%d"); val etperpollArr = construct("etperpoll%d"); val packetperArr = construct("packetper%d")
        val ssjrArr = construct("ssjr%d"); val shop_packArr = construct("shop_pack%d")
        val stdsetArr = construct("stdset%d"); val standard_editionArr = construct("standard_edition%d")
        val enhancedstaArr = construct("Enhancedsta%d"); val stdsealArr = construct("stdseal%d"); val stdsealtypeArr = construct("stdsealtype%d")
        val frontstaArr = construct("frontsta%d"); val soul_SpectralArr = construct("soul_Spectral%d")
        val soul_PlanetArr = construct("soul_Planet%d"); val soul_TarotArr = construct("soul_Tarot%d")
        val cdtArr = construct("cdt%d"); val VoucherArr = construct("Voucher%d"); val TagArr = construct("Tag%d")
    }
}

