package com.alexcova.perkeo.domain.engine

data class JokerCount(
    val joker: Item,
    var count: Int,
    var edition: Edition = Edition.NoEdition,
    var ante: Int,
    var source: String,
) {
    val id get() = joker.rawValue
}

class Run(val seed: String, val antes: List<Ante>) {

    fun contains(item: Item) = antes.any { it.contains(item) }

    fun jokers(): List<JokerCount> {
        val jokerMap = mutableMapOf<String, JokerCount>()
        for (ante in antes) {
            for (joker in ante.jokers()) {
                if (jokerMap.containsKey(joker.rawValue)) {
                    val j = jokerMap[joker.rawValue]!!
                    j.count++
                    if (joker.edition != Edition.NoEdition) j.edition = joker.edition
                    if (ante.ante < j.ante) { j.ante = ante.ante; j.source = joker.source ?: "" }
                } else {
                    jokerMap[joker.rawValue] = JokerCount(joker.item, 1, joker.edition, ante.ante, joker.source ?: "")
                }
            }
        }
        return jokerMap.values.sortedByDescending { it.joker.y }
    }

    fun tags(): List<Tag> = antes.flatMap { it.tags }.toSet().toList()

    fun vouchers(): List<Voucher> = antes.map { it.voucher }.toSet().toList()

    fun spectrals(): List<Spectral> {
        val set = mutableSetOf<Spectral>()
        for (ante in antes) {
            for (pack in ante.packs) {
                if (pack.kind == PackKind.Spectral) {
                    for (opt in pack.options) {
                        val s = opt.item as? Spectral ?: continue
                        set.add(s)
                    }
                }
            }
        }
        return set.toList()
    }

    fun hasLegendary(item: LegendaryJoker) = antes.any { it.hasLegendary(item) }
    fun hasVoucher(item: Voucher) = antes.any { it.hasVoucher(item) }
    fun hasJoker(item: Item) = antes.any { it.hasJoker(item) }
    fun hasJoker(item: Item, maxShopIndex: Int) = antes.any { it.hasInPack(item) || it.hasInShop(item, maxShopIndex) }

    val score: Int get() = RunScorer.score(this)
}

class Ante(val ante: Int, val functions: Functions) {
    val shopQueue: MutableList<SearchableItem> = mutableListOf()
    val shop: MutableSet<String> = mutableSetOf()
    var tags: MutableList<Tag> = mutableListOf()
    var boss: Boss = Boss.Amber_Acorn
    val packs: MutableList<Pack> = mutableListOf()
    var voucher: Voucher = Voucher.Nacho_Tong
    var legendaries: MutableList<JokerData>? = null

    fun hasInShop(item: Item, index: Int): Boolean {
        val limit = minOf(index, shopQueue.size)
        return (0 until limit).any { shopQueue[it].equals(item) }
    }

    fun hasVoucher(v: Voucher) = v == voucher
    fun hasJoker(joker: Item) = packs.any { it.containsOption(joker.rawValue) }

    fun jokers(): List<EditionItem> {
        val list = mutableListOf<EditionItem>()
        hasLegendary(LegendaryJoker.Perkeo) // trigger lazy init
        legendaries?.forEach { list.add(it.asEditionItem()) }
        for (pack in packs) {
            if (pack.kind == PackKind.Buffoon) pack.options.forEach { list.add(it.atSource("pack")) }
        }
        for (si in shopQueue) {
            if (si.item is Joker) list.add(si.asEditionItem("shop"))
        }
        return list.sortedBy { it.y }
    }

    fun addToQueue(value: ShopItem) {
        shop.add(value.item.rawValue)
        shopQueue.add(SearchableItem(value))
    }

    fun addPack(pack: Pack, options: List<EditionItem>) {
        pack.options.addAll(options)
        packs.add(pack)
    }

    fun hasLegendary(joker: Item): Boolean {
        if (legendaries != null) return legendaries!!.any { it.joker.rawValue == joker.rawValue }
        legendaries = mutableListOf()
        for (pack in packs) {
            if (pack.kind == PackKind.Buffoon || pack.kind == PackKind.Standard || pack.kind == PackKind.Celestial) continue
            for (opt in pack.options) {
                if (opt.item is LegendaryJoker) {
                    legendaries!!.add(JokerData(opt.item, JokerType.LEGENDARY, opt.edition, JokerStickers(), "${pack.kind}"))
                }
            }
        }
        return legendaries!!.any { it.joker.rawValue == joker.rawValue }
    }

    fun contains(item: Item): Boolean {
        if (hasLegendary(item)) return true
        if (shop.contains(item.rawValue)) return true
        if (hasInPack(item)) return true
        if (item is Voucher && item == voucher) return true
        if (item is Tag) return tags.any { it == item }
        return false
    }

    fun hasInPack(item: Item) = packs.any { it.containsOption(item.rawValue) }

    fun countInPack(item: Item): Int {
        var count = 0
        for (pack in packs) for (opt in pack.options) if (opt.item.rawValue == item.rawValue) count++
        return count
    }
}

