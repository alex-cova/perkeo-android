package com.alexcova.perkeo.ui.sprite

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.alexcova.perkeo.R
import com.alexcova.perkeo.domain.engine.*
import org.json.JSONArray

data class SpritePos(val x: Int, val y: Int)

data class SpriteInfo(
    val bitmap: Bitmap,
    val pos: SpritePos,
    val cellW: Int,
    val cellH: Int,
)

object SpriteSheets {
    @Volatile private var initialized = false
    lateinit var jokers: Bitmap; lateinit var tarots: Bitmap; lateinit var vouchers: Bitmap
    lateinit var bosses: Bitmap; lateinit var editions: Bitmap; lateinit var enhancers: Bitmap
    lateinit var cards: Bitmap; lateinit var tags: Bitmap; lateinit var chips: Bitmap

    private val jokerSprites = mutableMapOf<String, SpritePos>()
    private val tarotSprites = mutableMapOf<String, SpritePos>()
    private val voucherSprites = mutableMapOf<String, SpritePos>()
    private val tagSprites = mutableMapOf<String, SpritePos>()
    private val bossSprites = mutableMapOf<String, SpritePos>()

    fun init(context: Context) {
        if (initialized) return
        initialized = true
        val opts = BitmapFactory.Options().apply { inScaled = false }
        jokers = BitmapFactory.decodeResource(context.resources, R.drawable.sprite_jokers, opts)
        tarots = BitmapFactory.decodeResource(context.resources, R.drawable.sprite_tarots, opts)
        vouchers = BitmapFactory.decodeResource(context.resources, R.drawable.sprite_vouchers, opts)
        bosses = BitmapFactory.decodeResource(context.resources, R.drawable.sprite_bosses, opts)
        editions = BitmapFactory.decodeResource(context.resources, R.drawable.sprite_editions, opts)
        enhancers = BitmapFactory.decodeResource(context.resources, R.drawable.sprite_enhancers, opts)
        cards = BitmapFactory.decodeResource(context.resources, R.drawable.sprite_cards, opts)
        tags = BitmapFactory.decodeResource(context.resources, R.drawable.sprite_tags, opts)
        chips = BitmapFactory.decodeResource(context.resources, R.drawable.sprite_chips, opts)

        loadJson(context, "jokers.json") { name, x, y -> jokerSprites[name] = SpritePos(x, y) }
        loadJson(context, "tarots.json") { name, x, y -> tarotSprites[name] = SpritePos(x, y) }
        loadJson(context, "vouchers.json") { name, x, y -> voucherSprites[name] = SpritePos(x, y) }
        loadJson(context, "tags.json") { name, x, y -> tagSprites[name] = SpritePos(x, y) }
        loadJson(context, "bosses.json") { name, x, y -> bossSprites[name] = SpritePos(x, y) }
    }

    private fun loadJson(context: Context, file: String, block: (String, Int, Int) -> Unit) {
        try {
            val json = JSONArray(context.assets.open(file).bufferedReader().readText())
            for (i in 0 until json.length()) {
                val obj = json.getJSONObject(i)
                val pos = obj.getJSONObject("pos")
                block(obj.getString("name"), pos.getInt("x"), pos.getInt("y"))
            }
        } catch (_: Exception) {}
    }

    fun getSpriteInfo(item: Item, edition: Edition? = null): SpriteInfo {
        val name = item.rawValue

        if (item is Card) {
            return SpriteInfo(cards, SpritePos(item.rank.index(), item.suit.index()), 71, 95)
        }

        if (name == Specials.BLACKHOLE.rawValue) return SpriteInfo(tarots, SpritePos(9, 3), 71, 95)
        if (name == Specials.THE_SOUL.rawValue) return SpriteInfo(tarots, SpritePos(2, 2), 71, 95)

        jokerSprites[name]?.let { return SpriteInfo(jokers, it, 71, 95) }

        // Legendary jokers have special multi-row positions
        val legendary = LegendaryJoker.entries.find { it.rawValue == name }
        if (legendary != null) {
            val x = 3 + legendary.ordinal
            return SpriteInfo(jokers, SpritePos(x, 8), 71, 95)
        }

        tarotSprites[name]?.let { return SpriteInfo(tarots, it, 71, 95) }
        voucherSprites[name]?.let { return SpriteInfo(vouchers, it, 71, 95) }

        // Tags lookup by "X Tag" name pattern
        tagSprites["$name Tag"]?.let { return SpriteInfo(tags, it, 34, 34) }

        bossSprites[name]?.let { return SpriteInfo(bosses, it, 34, 34) }

        // Deck sprites on enhancers sheet
        val deck = Deck.entries.find { it.rawValue == name }
        if (deck != null) {
            val pos = when (deck) {
                Deck.RED_DECK -> SpritePos(0, 0); Deck.BLUE_DECK -> SpritePos(0, 2); Deck.GREEN_DECK -> SpritePos(2, 2)
                Deck.YELLOW_DECK -> SpritePos(1, 2); Deck.BLACK_DECK -> SpritePos(3, 2); Deck.MAGIC_DECK -> SpritePos(0, 3)
                Deck.NEBULA_DECK -> SpritePos(3, 0); Deck.GHOST_DECK -> SpritePos(6, 2); Deck.ABANDONED_DECK -> SpritePos(3, 3)
                Deck.CHECKERED_DECK -> SpritePos(1, 3); Deck.ZODIAC_DECK -> SpritePos(4, 3); Deck.PAINTED_DECK -> SpritePos(3, 4)
                Deck.ANAGLYPH_DECK -> SpritePos(2, 4); Deck.PLASMA_DECK -> SpritePos(4, 2); Deck.ERRATIC_DECK -> SpritePos(2, 3)
            }
            return SpriteInfo(enhancers, pos, 71, 95)
        }

        // Stake / chip sprites
        val stake = Stake.entries.find { it.rawValue == name }
        if (stake != null) {
            val pos = when (stake) {
                Stake.White_Stake -> SpritePos(0, 0); Stake.Red_Stake -> SpritePos(1, 0); Stake.Green_Stake -> SpritePos(2, 0)
                Stake.Blue_Stake -> SpritePos(3, 0); Stake.Black_Stake -> SpritePos(4, 0); Stake.Purple_Stake -> SpritePos(0, 1)
                Stake.Orange_Stake -> SpritePos(1, 1); Stake.Gold_Stake -> SpritePos(2, 1)
            }
            return SpriteInfo(chips, pos, 29, 29)
        }

        // Fallback
        return SpriteInfo(vouchers, SpritePos(7, 3), 34, 45)
    }

    fun getEnhancementBitmap(enhancement: Enhancement?): Bitmap? {
        if (!initialized) return null
        val (x, y) = when (enhancement) {
            Enhancement.Luck -> 4 to 1
            Enhancement.Bonus -> 1 to 1
            Enhancement.Wild -> 3 to 1
            Enhancement.Gold -> 6 to 0
            Enhancement.Stone -> 5 to 0
            Enhancement.Steel -> 6 to 1
            Enhancement.Glass -> 5 to 1
            Enhancement.Mult -> 2 to 1
            else -> 1 to 0
        }
        return cropBitmap(enhancers, SpritePos(x, y), 71, 95)
    }

    fun getSealBitmap(seal: Seal): Bitmap? {
        if (!initialized) return null
        val (x, y) = when (seal) {
            Seal.RedSeal -> 5 to 4
            Seal.GoldSeal -> 2 to 0
            Seal.PurpleSeal -> 4 to 4
            Seal.BlueSeal -> 6 to 4
            else -> return null
        }
        return cropBitmap(enhancers, SpritePos(x, y), 71, 95)
    }

    fun getEditionBitmap(edition: Edition, cellW: Int = 71, cellH: Int = 95): Bitmap? {
        if (!initialized) return null
        val idx = when (edition) { Edition.Foil -> 1; Edition.Holographic -> 2; Edition.Polychrome -> 3; else -> return null }
        val x = idx * cellW
        // Editions sheet may be 1px shorter than sprite cellH (94 vs 95); clamp to actual height
        val actualH = minOf(cellH, editions.height)
        return if (x + cellW <= editions.width && actualH > 0)
            Bitmap.createBitmap(editions, x, 0, cellW, actualH)
        else null
    }

    fun cropBitmap(sheet: Bitmap, pos: SpritePos, cellW: Int, cellH: Int): Bitmap? {
        val px = pos.x * cellW; val py = pos.y * cellH
        if (px + cellW > sheet.width || py + cellH > sheet.height) return null
        return Bitmap.createBitmap(sheet, px, py, cellW, cellH)
    }
}

