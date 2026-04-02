package com.alexcova.perkeo.domain.engine

object BalatroAnalyzer {
    private const val CHARACTERS = "123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    fun generateRandomString(): String = buildString { repeat(8) { append(CHARACTERS.random()) } }

    fun performAnalysis(seed: String, maxDepth: Int = 8, startingAnte: Int = 1,
                        deck: Deck = Deck.RED_DECK, stake: Stake = Stake.White_Stake,
                        showman: Boolean = false, disabledItems: List<Item> = emptyList(),
                        autoBuyVoucher: Boolean = false): Run {
        val cards = IntArray(maxDepth) { 52 }.also { it[0] = 15 }
        val inst = Functions(seed, maxDepth)
        inst.setParams(InstanceParams(deck, stake, showman, 1))
        inst.initLocks(1, false, true)
        inst.firstLock()
        for (option in disabledItems) inst.lock(option)
        inst.setDeck(deck)

        val antes = mutableListOf<Ante>()
        for (a in startingAnte..maxDepth) {
            val play = Ante(a, inst)
            antes.add(play)
            inst.initUnlocks(a, false)

            play.boss = inst.nextBoss(a)

            val voucher = inst.nextVoucher(a)
            play.voucher = voucher
            if (autoBuyVoucher) inst.lock(voucher)
            for (i in Functions.VOUCHERS.indices step 2) {
                if (Functions.VOUCHERS[i] == voucher) {
                    if (disabledItems.none { it.rawValue == Functions.VOUCHERS[i + 1].rawValue }) {
                        inst.unlock(Functions.VOUCHERS[i + 1])
                    }
                }
            }

            play.tags.add(inst.nextTag(a))
            play.tags.add(inst.nextTag(a))

            val shopCount = cards.getOrElse(a - 1) { 52 }
            for (j in 0 until shopCount) play.addToQueue(inst.nextShopItem(a))

            val numPacks = if (a == 1) 4 else 6
            for (k in 0 until numPacks) {
                val pack = inst.nextPack(a)
                val packInfo = inst.packInfo(pack)
                val options = mutableListOf<EditionItem>()

                when (pack.kind) {
                    PackKind.Celestial -> {
                        val c = inst.nextCelestialPack(packInfo.size, a)
                        for (item in c) options.add(EditionItem(item))
                    }
                    PackKind.Arcana -> {
                        val c = inst.nextArcanaPack(packInfo.size, a)
                        for (item in c) options.add(if (item is EditionItem) item else EditionItem(item))
                    }
                    PackKind.Spectral -> {
                        val c = inst.nextSpectralPack(packInfo.size, a)
                        for (item in c) options.add(if (item is EditionItem) item else EditionItem(item))
                    }
                    PackKind.Buffoon -> {
                        val c = inst.nextBuffoonPack(packInfo.size, a)
                        for (jd in c) options.add(EditionItem(BalatroAnalyzer.getEdition(jd), jd.joker))
                    }
                    PackKind.Standard -> {
                        val c = inst.nextStandardPack(packInfo.size, a)
                        for (card in c) options.add(EditionItem(card))
                    }
                }
                play.addPack(packInfo, options)
            }
        }
        return Run(seed, antes)
    }

    fun getEdition(joker: JokerData): Edition {
        var ed: Edition? = null
        if (joker.stickers.eternal) ed = Edition.Eternal
        if (joker.stickers.perishable) ed = Edition.Perishable
        if (joker.stickers.rental) ed = Edition.Rental
        if (joker.edition != Edition.NoEdition) ed = joker.edition
        return ed ?: Edition.NoEdition
    }

    fun configureForSpeedAnalysis(seed: String, maxDepth: Int, startingAnte: Int,
                                   selections: List<Item>): Run {
        // Minimal analysis for finder - only enable needed categories
        val inst = Functions(seed, maxDepth)
        inst.setParams(InstanceParams(Deck.RED_DECK, Stake.White_Stake, false, 1))
        inst.initLocks(1, false, true)
        inst.firstLock()
        inst.setDeck(Deck.RED_DECK)

        val analyzeBoss = selections.any { it is Boss }
        val analyzeVoucher = selections.any { it is Voucher }
        val analyzeTags = selections.any { it is Tag }
        val analyzeShop = true
        val analyzeArcana = selections.any { it is Tarot || it is LegendaryJoker }
        val analyzeCelestial = selections.any { it is Planet || it is Spectral }
        val analyzeSpectral = selections.any { it is Spectral || it is LegendaryJoker }
        val analyzeBuffoon = selections.any { it is Joker }
        val analyzeStandard = selections.any { it is Cards }

        val antes = mutableListOf<Ante>()
        for (a in startingAnte..maxDepth) {
            val play = Ante(a, inst)
            antes.add(play)
            inst.initUnlocks(a, false)
            if (analyzeBoss) play.boss = inst.nextBoss(a)
            if (analyzeVoucher) { val v = inst.nextVoucher(a); play.voucher = v }
            if (analyzeTags) { play.tags.add(inst.nextTag(a)); play.tags.add(inst.nextTag(a)) }
            if (analyzeShop) for (j in 0 until 15) play.addToQueue(inst.nextShopItem(a))

            val numPacks = if (a == 1) 4 else 6
            for (k in 0 until numPacks) {
                val pack = inst.nextPack(a); val packInfo = inst.packInfo(pack); val options = mutableListOf<EditionItem>()
                when (pack.kind) {
                    PackKind.Celestial -> if (analyzeCelestial) { val c = inst.nextCelestialPack(packInfo.size, a); for (item in c) options.add(EditionItem(item)) }
                    PackKind.Arcana -> if (analyzeArcana) { val c = inst.nextArcanaPack(packInfo.size, a); for (item in c) options.add(if (item is EditionItem) item else EditionItem(item)) }
                    PackKind.Spectral -> if (analyzeSpectral) { val c = inst.nextSpectralPack(packInfo.size, a); for (item in c) options.add(if (item is EditionItem) item else EditionItem(item)) }
                    PackKind.Buffoon -> if (analyzeBuffoon) { val c = inst.nextBuffoonPack(packInfo.size, a); for (jd in c) options.add(EditionItem(getEdition(jd), jd.joker)) }
                    PackKind.Standard -> if (analyzeStandard) { val c = inst.nextStandardPack(packInfo.size, a); for (card in c) options.add(EditionItem(card)) }
                }
                play.addPack(packInfo, options)
            }
        }
        return Run(seed, antes)
    }
}

