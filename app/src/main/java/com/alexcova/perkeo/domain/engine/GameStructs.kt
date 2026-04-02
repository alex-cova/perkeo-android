package com.alexcova.perkeo.domain.engine

class JokerStickers {
    var eternal = false
    var perishable = false
    var rental = false
}

class JokerData {
    var joker: Item = CommonJoker.Joker
    var type: JokerType = JokerType.COMMON
    var edition: Edition = Edition.NoEdition
    var stickers: JokerStickers = JokerStickers()
    var source: String? = null

    constructor()
    constructor(joker: Item, type: JokerType, edition: Edition, stickers: JokerStickers, source: String? = null) {
        this.joker = joker; this.type = type; this.edition = edition; this.stickers = stickers; this.source = source
    }

    fun computeEdition(): Edition {
        if (edition != Edition.NoEdition) return edition
        if (stickers.rental) return Edition.Rental
        if (stickers.eternal) return Edition.Eternal
        if (stickers.perishable) return Edition.Perishable
        return edition
    }

    fun asEditionItem() = EditionItem(computeEdition(), joker, source ?: "")
}

class Card(val base: Cards, val enhancement: Enhancement?, val edition: Edition, val seal: Seal) : Item {
    override val rawValue get() = if (enhancement == Enhancement.Stone) "Stone" else "${rank.rawValue} ${suit.rawValue}"
    override val bitOrdinal get() = base.ordinal
    override val y get() = base.y

    val suit: Suit get() = when (base.rawValue[0]) {
        'C' -> Suit.Clubs; 'H' -> Suit.Hearts; 'D' -> Suit.Diamonds; else -> Suit.Spades
    }
    val rank: Rank get() = when (base.rawValue[2]) {
        'T' -> Rank.r_10; 'J' -> Rank.Jack; 'Q' -> Rank.Queen; 'K' -> Rank.King; '3' -> Rank.r_3; '4' -> Rank.r_4
        '5' -> Rank.r_5; '6' -> Rank.r_6; '7' -> Rank.r_7; '8' -> Rank.r_8; '9' -> Rank.r_9; 'A' -> Rank.Ace; else -> Rank.r_2
    }
}

class EditionItem(val edition: Edition, val item: Item, val source: String? = null, val seal: Seal? = null) : Item {
    override val rawValue get() = item.rawValue
    override val bitOrdinal get() = item.bitOrdinal
    override val y get() = item.y
    fun atSource(src: String) = EditionItem(edition, item, src, seal)
    constructor(item: Item) : this(Edition.NoEdition, item, null)
    constructor(card: Card) : this(card.edition, card, null, card.seal)
}

class Pack(val type: PackType, val size: Int, val choices: Int) {
    var options: MutableList<EditionItem> = mutableListOf()
    val kind get() = type.kind
    fun containsOption(name: String) = options.any { it.item.rawValue == name }
}

class ShopInstance(val jokerRate: Double, val tarotRate: Double, val planetRate: Double,
                   val playingCardRate: Double, val spectralRate: Double) {
    fun getTotalRate() = jokerRate + tarotRate + planetRate + playingCardRate + spectralRate
}

class ShopItem() {
    var type: ItemType = ItemType.Tarot
    var item: Item = Tarot.The_Fool
    var jokerData: JokerData? = null
    constructor(type: ItemType, item: Item) : this() { this.type = type; this.item = item; this.jokerData = JokerData() }
    constructor(type: ItemType, item: Item, jd: JokerData) : this() { this.type = type; this.item = item; this.jokerData = jd }
    val edition: Edition get() = jokerData?.computeEdition() ?: Edition.NoEdition
}

class InstanceParams {
    var deck: Deck = Deck.RED_DECK
    var stake: Stake = Stake.White_Stake
    var showman: Boolean = false
    var sixesFactor: Int = 1
    var vouchers: MutableSet<Voucher> = mutableSetOf()
    constructor()
    constructor(deck: Deck, stake: Stake, showman: Boolean, sixesFactor: Int) {
        this.deck = deck; this.stake = stake; this.showman = showman; this.sixesFactor = sixesFactor
    }
}

class SearchableItem(val item: Item, val edition: Edition?) {
    constructor(shopItem: ShopItem) : this(shopItem.item, shopItem.edition)
    fun equals(other: Item) = item.equals(other)
    fun asEditionItem(source: String? = null) = EditionItem(edition ?: Edition.NoEdition, item, source)
}

