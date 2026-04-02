package com.alexcova.perkeo.domain.engine

enum class PackKind { Arcana, Celestial, Standard, Buffoon, Spectral }

interface Item {
    val rawValue: String
    // bitOrdinal is a logical index used for bitsets / stored indices and mirrors the Swift ordinal semantics
    val bitOrdinal: Int
    val y: Int
    fun isRetry() = rawValue == "RETRY" || rawValue == "RETRY2"
    fun equals(other: Item) = rawValue == other.rawValue
}

interface Joker { val jokerType: JokerType }
interface StoredItem { val index: Int }

enum class JokerType(val rawValue: String, val rarity: Int) {
    LEGENDARY("Legendary", 4), RARE("Rare", 3), UNCOMMON("UnCommon", 2), COMMON("Common", 1);
    companion object { fun allCases() = entries }
}

enum class PackType(override val rawValue: String, val value: Double, val kind: PackKind) : Item {
    RETRY("RETRY", 22.42, PackKind.Standard),
    Arcana_Pack("Arcana Pack", 4.0, PackKind.Arcana),
    Jumbo_Arcana_Pack("Jumbo Arcana Pack", 2.0, PackKind.Arcana),
    Mega_Arcana_Pack("Mega Arcana Pack", 0.5, PackKind.Arcana),
    Celestial_Pack("Celestial Pack", 4.0, PackKind.Celestial),
    Jumbo_Celestial_Pack("Jumbo Celestial Pack", 2.0, PackKind.Celestial),
    Mega_Celestial_Pack("Mega Celestial Pack", 0.5, PackKind.Celestial),
    Standard_Pack("Standard Pack", 4.0, PackKind.Standard),
    Jumbo_Standard_Pack("Jumbo Standard Pack", 2.0, PackKind.Standard),
    Mega_Standard_Pack("Mega Standard Pack", 0.5, PackKind.Standard),
    Buffoon_Pack("Buffoon Pack", 1.2, PackKind.Buffoon),
    Jumbo_Buffoon_Pack("Jumbo Buffoon Pack", 0.6, PackKind.Buffoon),
    Mega_Buffoon_Pack("Mega Buffoon Pack", 0.15, PackKind.Buffoon),
    Spectral_Pack("Spectral Pack", 0.6, PackKind.Spectral),
    Jumbo_Spectral_Pack("Jumbo Spectral Pack", 0.3, PackKind.Spectral),
    Mega_Spectral_Pack("Mega Spectral Pack", 0.07, PackKind.Spectral);

    override val bitOrdinal: Int get() = 0
    override val y get() = 0
    val isMega get() = name.startsWith("Mega_")
    val isJumbo get() = name.startsWith("Jumbo_")
    val isBuffoon get() = kind == PackKind.Buffoon
    val isSpectral get() = kind == PackKind.Spectral
}

enum class Seal(override val rawValue: String) : Item {
    NoSeal("No Seal"), RedSeal("Red Seal"), BlueSeal("Blue Seal"), GoldSeal("Gold Seal"), PurpleSeal("Purple Seal");
    override val bitOrdinal: Int get() = 0
    override val y get() = 0
}

enum class Edition(override val rawValue: String, override val bitOrdinal: Int, val multiplier: Float) : Item {
    Negative("Negative", 0, 2.0f), Polychrome("Polychrome", 1, 1.5f),
    Holographic("Holographic", 2, 1.2f), Foil("Foil", 3, 1.1f),
    NoEdition("No Edition", 4, 0.0f), Eternal("Eternal", 5, 0.0f),
    Perishable("Perishable", 6, 0.0f), Rental("Rental", 7, 0.0f);
    override val y get() = -1
    val next: Edition get() = when(this) { NoEdition -> Negative; Negative -> Foil; Foil -> Holographic; Holographic -> Polychrome; else -> NoEdition }
}

enum class ItemType(override val rawValue: String) : Item {
    Joker("Joker"), Tarot("Tarot"), Planet("Planet"), Spectral("Spectral"), PlayingCard("Playing Card");
    override val bitOrdinal: Int get() = 0
    override val y get() = 0
}

enum class Stake(override val rawValue: String) : Item {
    White_Stake("White Stake"), Red_Stake("Red Stake"), Green_Stake("Green Stake"),
    Black_Stake("Black Stake"), Blue_Stake("Blue Stake"), Purple_Stake("Purple Stake"),
    Orange_Stake("Orange Stake"), Gold_Stake("Gold Stake");
    override val bitOrdinal: Int get() = 0
    override val y get() = 0
}

enum class Specials(override val rawValue: String, override val bitOrdinal: Int) : Item, StoredItem {
    BLACKHOLE("Black Hole", 0), THE_SOUL("The Soul", 1);
    override val y get() = 9
    override val index get() = 188 + bitOrdinal
}

enum class Voucher(override val rawValue: String, override val bitOrdinal: Int) : Item, StoredItem {
    Overstock("Overstock", 0), Overstock_Plus("Overstock Plus", 1), Clearance_Sale("Clearance Sale", 2),
    Liquidation("Liquidation", 3), Hone("Hone", 4), Glow_Up("Glow Up", 5), Reroll_Surplus("Reroll Surplus", 6),
    Reroll_Glut("Reroll Glut", 7), Crystal_Ball("Crystal Ball", 8), Omen_Globe("Omen Globe", 9),
    Telescope("Telescope", 10), Observatory("Observatory", 11), Grabber("Grabber", 12), Nacho_Tong("Nacho Tong", 13),
    Wasteful("Wasteful", 14), Recyclomancy("Recyclomancy", 15), Tarot_Merchant("Tarot Merchant", 16),
    Tarot_Tycoon("Tarot Tycoon", 17), Planet_Merchant("Planet Merchant", 18), Planet_Tycoon("Planet Tycoon", 19),
    Seed_Money("Seed Money", 20), Money_Tree("Money Tree", 21), Blank("Blank", 22), Antimatter("Antimatter", 23),
    Magic_Trick("Magic Trick", 24), Illusion("Illusion", 25), Hieroglyph("Hieroglyph", 26),
    Petroglyph("Petroglyph", 27), Directors_Cut("Director's Cut", 28), Retcon("Retcon", 29),
    Paint_Brush("Paint Brush", 30), Palett("Palette", 31);
    override val y get() = 7
    override val index get() = 32 + bitOrdinal
    companion object { fun allCases() = entries }
}

enum class Version(val code: Int) { v_100n(10014), v_101c(10103), v_101f(10106) }

enum class UnCommonJoker(override val rawValue: String, override val bitOrdinal: Int) : Item, Joker, StoredItem {
    Joker_Stencil("Joker Stencil", 0), Four_Fingers("Four Fingers", 1), Mime("Mime", 2),
    Ceremonial_Dagger("Ceremonial Dagger", 3), Marble_Joker("Marble Joker", 4), Loyalty_Card("Loyalty Card", 5),
    Dusk("Dusk", 6), Fibonacci("Fibonacci", 7), Steel_Joker("Steel Joker", 8), Hack("Hack", 9),
    Pareidolia("Pareidolia", 10), Space_Joker("Space Joker", 11), Burglar("Burglar", 12), Blackboard("Blackboard", 13),
    Sixth_Sense("Sixth Sense", 14), Constellation("Constellation", 15), Hiker("Hiker", 16), Card_Sharp("Card Sharp", 17),
    Madness("Madness", 18), Seance("Seance", 19), Vampire("Vampire", 20), Shortcut("Shortcut", 21),
    Hologram("Hologram", 22), Cloud_9("Cloud 9", 23), Rocket("Rocket", 24), Midas_Mask("Midas Mask", 25),
    Luchador("Luchador", 26), Gift_Card("Gift Card", 27), Turtle_Bean("Turtle Bean", 28), Erosion("Erosion", 29),
    To_the_Moon("To the Moon", 30), Stone_Joker("Stone Joker", 31), Lucky_Cat("Lucky Cat", 32), Bull("Bull", 33),
    Diet_Cola("Diet Cola", 34), Trading_Card("Trading Card", 35), Flash_Card("Flash Card", 36),
    Spare_Trousers("Spare Trousers", 37), Ramen("Ramen", 38), Seltzer("Seltzer", 39), Castle("Castle", 40),
    Mr_Bones("Mr. Bones", 41), Acrobat("Acrobat", 42), Sock_and_Buskin("Sock and Buskin", 43),
    Troubadour("Troubadour", 44), Certificate("Certificate", 45), Smeared_Joker("Smeared Joker", 46),
    Throwback("Throwback", 47), Rough_Gem("Rough Gem", 48), Bloodstone("Bloodstone", 49),
    Arrowhead("Arrowhead", 50), Onyx_Agate("Onyx Agate", 51), Glass_Joker("Glass Joker", 52),
    Showman("Showman", 53), Flower_Pot("Flower Pot", 54), Merry_Andy("Merry Andy", 55),
    Oops_All_6s("Oops! All 6s", 56), The_Idol("The Idol", 57), Seeing_Double("Seeing Double", 58),
    Matador("Matador", 59), Satellite("Satellite", 60), Cartomancer("Cartomancer", 61),
    Astronomer("Astronomer", 62), Bootstraps("Bootstraps", 63);
    override val y get() = 1
    override val jokerType get() = JokerType.UNCOMMON
    override val index get() = 64 + bitOrdinal
    companion object { fun allCases() = entries }
}

enum class Tarot(override val rawValue: String, override val bitOrdinal: Int) : Item {
    The_Fool("The Fool", 0), The_Magician("The Magician", 1), The_High_Priestess("The High Priestess", 2),
    The_Empress("The Empress", 3), The_Emperor("The Emperor", 4), The_Hierophant("The Hierophant", 5),
    The_Lovers("The Lovers", 6), The_Chariot("The Chariot", 7), Justice("Justice", 8), The_Hermit("The Hermit", 9),
    The_Wheel_of_Fortune("The Wheel of Fortune", 10), Strength("Strength", 11), The_Hanged_Man("The Hanged Man", 12),
    Death("Death", 13), Temperance("Temperance", 14), The_Devil("The Devil", 15), The_Tower("The Tower", 16),
    The_Star("The Star", 17), The_Moon("The Moon", 18), The_Sun("The Sun", 19), Judgement("Judgement", 20),
    The_World("The World", 21);
    override val y get() = 4
    companion object { fun allCases() = entries }
}

enum class Tag(override val rawValue: String, override val bitOrdinal: Int) : Item, StoredItem {
    Uncommon_Tag("Uncommon", 0), Rare_Tag("Rare", 1), Negative_Tag("Negative", 2),
    Foil_Tag("Foil", 3), Holographic_Tag("Holographic", 4), Polychrome_Tag("Polychrome", 5),
    Investment_Tag("Investment", 6), Voucher_Tag("Voucher", 7), Boss_Tag("Boss", 8),
    Standard_Tag("Standard", 9), Charm_Tag("Charm", 10), Meteor_Tag("Meteor", 11),
    Buffoon_Tag("Buffoon", 12), Handy_Tag("Handy", 13), Garbage_Tag("Garbage", 14),
    Ethereal_Tag("Ethereal", 15), Coupon_Tag("Coupon", 16), Double_Tag("Double", 17),
    Juggle_Tag("Juggle", 18), D6_Tag("D6", 19), Top_up_Tag("Top-up", 20),
    Speed_Tag("Speed", 21), Orbital_Tag("Orbital", 22), Economy_Tag("Economy", 23);
    override val y get() = 8
    override val index get() = 231 + bitOrdinal
    companion object { fun allCases() = entries }
}

enum class Spectral(override val rawValue: String, override val bitOrdinal: Int) : Item, StoredItem {
    Familiar("Familiar", 0), Grim("Grim", 1), Incantation("Incantation", 2), Talisman("Talisman", 3),
    Aura("Aura", 4), Wraith("Wraith", 5), Sigil("Sigil", 6), Ouija("Ouija", 7), Ectoplasm("Ectoplasm", 8),
    Immolate("Immolate", 9), Ankh("Ankh", 10), Deja_Vu("Deja Vu", 11), Hex("Hex", 12), Trance("Trance", 13),
    Medium("Medium", 14), Cryptid("Cryptid", 15), RETRY("RETRY", 16), RETRY2("RETRY2", 17);
    override val y get() = 5
    override val index get() = 195 + bitOrdinal
    companion object { fun allCases() = entries.filter { !it.isRetry() } }
}

enum class RareJoker(override val rawValue: String, override val bitOrdinal: Int) : Item, Joker, StoredItem {
    DNA("DNA", 0), Vagabond("Vagabond", 1), Baron("Baron", 2), Obelisk("Obelisk", 3),
    Baseball_Card("Baseball Card", 4), Ancient_Joker("Ancient Joker", 5), Campfire("Campfire", 6),
    Blueprint("Blueprint", 7), Wee_Joker("Wee Joker", 8), Hit_the_Road("Hit the Road", 9),
    The_Duo("The Duo", 10), The_Trio("The Trio", 11), The_Family("The Family", 12),
    The_Order("The Order", 13), The_Tribe("The Tribe", 14), Stuntman("Stuntman", 15),
    Invisible_Joker("Invisible Joker", 16), Brainstorm("Brainstorm", 17), Drivers_License("Drivers License", 18),
    Burnt_Joker("Burnt Joker", 19);
    override val y get() = 2
    override val jokerType get() = JokerType.RARE
    override val index get() = 211 + bitOrdinal
    companion object { fun allCases() = entries }
}

enum class Planet(override val rawValue: String, override val bitOrdinal: Int) : Item {
    Mercury("Mercury", 0), Venus("Venus", 1), Earth("Earth", 2), Mars("Mars", 3),
    Jupiter("Jupiter", 4), Saturn("Saturn", 5), Uranus("Uranus", 6), Neptune("Neptune", 7),
    Pluto("Pluto", 8), Planet_X("Planet X", 9), Ceres("Ceres", 10), Eri("Eris", 11);
    override val y get() = 3
    companion object { fun allCases() = entries }
}

enum class LegendaryJoker(override val rawValue: String, override val bitOrdinal: Int) : Item, Joker, StoredItem {
    Canio("Canio", 0), Triboulet("Triboulet", 1), Yorick("Yorick", 2), Chicot("Chicot", 3), Perkeo("Perkeo", 4);
    override val y get() = 10
    override val jokerType get() = JokerType.LEGENDARY
    override val index get() = 190 + bitOrdinal
    companion object { fun allCases() = entries }
}

enum class Enhancement(override val rawValue: String) : Item {
    Bonus("Bonus"), Mult("Mult"), Wild("Wild"), Glass("Glass"), Steel("Steel"), Stone("Stone"), Gold("Gold"), Luck("Lucky");
    override val bitOrdinal: Int get() = 0
    override val y get() = 0
    companion object { fun allCases() = entries }
}

enum class Deck(override val rawValue: String) : Item {
    RED_DECK("Red Deck"), BLUE_DECK("Blue Deck"), YELLOW_DECK("Yellow Deck"), GREEN_DECK("Green Deck"),
    BLACK_DECK("Black Deck"), MAGIC_DECK("Magic Deck"), NEBULA_DECK("Nebula Deck"), GHOST_DECK("Ghost Deck"),
    ABANDONED_DECK("Abandoned Deck"), CHECKERED_DECK("Checkered Deck"), ZODIAC_DECK("Zodiac Deck"),
    PAINTED_DECK("Painted Deck"), ANAGLYPH_DECK("Anaglyph Deck"), PLASMA_DECK("Plasma Deck"), ERRATIC_DECK("Erratic Deck");
    override val bitOrdinal: Int get() = 0
    override val y get() = 0
    companion object { fun allCases() = entries }
}

enum class CommonJoker(override val rawValue: String, override val bitOrdinal: Int) : Item, Joker, StoredItem {
    Joker("Joker", 0), Greedy_Joker("Greedy Joker", 1), Lusty_Joker("Lusty Joker", 2),
    Wrathful_Joker("Wrathful Joker", 3), Gluttonous_Joker("Gluttonous Joker", 4), Jolly_Joker("Jolly Joker", 5),
    Zany_Joker("Zany Joker", 6), Mad_Joker("Mad Joker", 7), Crazy_Joker("Crazy Joker", 8),
    Droll_Joker("Droll Joker", 9), Sly_Joker("Sly Joker", 10), Wily_Joker("Wily Joker", 11),
    Clever_Joker("Clever Joker", 12), Devious_Joker("Devious Joker", 13), Crafty_Joker("Crafty Joker", 14),
    Half_Joker("Half Joker", 15), Credit_Card("Credit Card", 16), Banner("Banner", 17),
    Mystic_Summit("Mystic Summit", 18), Ball("8 Ball", 19), Misprint("Misprint", 20), Raised_Fist("Raised Fist", 21),
    Chaos_the_Clown("Chaos the Clown", 22), Scary_Face("Scary Face", 23), Abstract_Joker("Abstract Joker", 24),
    Delayed_Gratification("Delayed Gratification", 25), Gros_Michel("Gros Michel", 26),
    Even_Steven("Even Steven", 27), Odd_Todd("Odd Todd", 28), Scholar("Scholar", 29),
    Business_Card("Business Card", 30), Supernova("Supernova", 31), Ride_the_Bus("Ride the Bus", 32),
    Egg("Egg", 33), Runner("Runner", 34), Ice_Cream("Ice Cream", 35), Splash("Splash", 36),
    Blue_Joker("Blue Joker", 37), Faceless_Joker("Faceless Joker", 38), Green_Joker("Green Joker", 39),
    Superposition("Superposition", 40), To_Do_List("To Do List", 41), Cavendish("Cavendish", 42),
    Red_Card("Red Card", 43), Square_Joker("Square Joker", 44), Riffraff("Riff-raff", 45),
    Photograph("Photograph", 46), Reserved_Parking("Reserved Parking", 47), Mail_In_Rebate("Mail In Rebate", 48),
    Hallucination("Hallucination", 49), Fortune_Teller("Fortune Teller", 50), Juggler("Juggler", 51),
    Drunkard("Drunkard", 52), Golden_Joker("Golden Joker", 53), Popcorn("Popcorn", 54),
    Walkie_Talkie("Walkie Talkie", 55), Smiley_Face("Smiley Face", 56), Golden_Ticket("Golden Ticket", 57),
    Swashbuckler("Swashbuckler", 58), Hanging_Chad("Hanging Chad", 59), Shoot_the_Moon("Shoot the Moon", 60);
    override val y get() = 0
    override val jokerType get() = JokerType.COMMON
    override val index get() = 128 + bitOrdinal
    companion object { fun allCases() = entries }
}

enum class Cards(override val rawValue: String) : Item {
    C_2("C_2"), C_3("C_3"), C_4("C_4"), C_5("C_5"), C_6("C_6"), C_7("C_7"), C_8("C_8"), C_9("C_9"),
    C_A("C_A"), C_J("C_J"), C_K("C_K"), C_Q("C_Q"), C_T("C_T"),
    D_2("D_2"), D_3("D_3"), D_4("D_4"), D_5("D_5"), D_6("D_6"), D_7("D_7"), D_8("D_8"), D_9("D_9"),
    D_A("D_A"), D_J("D_J"), D_K("D_K"), D_Q("D_Q"), D_T("D_T"),
    H_2("H_2"), H_3("H_3"), H_4("H_4"), H_5("H_5"), H_6("H_6"), H_7("H_7"), H_8("H_8"), H_9("H_9"),
    H_A("H_A"), H_J("H_J"), H_K("H_K"), H_Q("H_Q"), H_T("H_T"),
    S_2("S_2"), S_3("S_3"), S_4("S_4"), S_5("S_5"), S_6("S_6"), S_7("S_7"), S_8("S_8"), S_9("S_9"),
    S_A("S_A"), S_J("S_J"), S_K("S_K"), S_Q("S_Q"), S_T("S_T");
    override val bitOrdinal: Int get() = 0
    override val y get() = 0
    companion object { fun allCases() = entries }
}

enum class Boss(override val rawValue: String, override val bitOrdinal: Int) : Item {
    The_Arm("The Arm", 0), The_Club("The Club", 1), The_Eye("The Eye", 2), Amber_Acorn("Amber Acorn", 3),
    Cerulean_Bell("Cerulean Bell", 4), Crimson_Heart("Crimson Heart", 5), Verdant_Leaf("Verdant Leaf", 6),
    Violet_Vessel("Violet Vessel", 7), The_Fish("The Fish", 8), The_Flint("The Flint", 9),
    The_Goad("The Goad", 10), The_Head("The Head", 11), The_Hook("The Hook", 12), The_House("The House", 13),
    The_Manacle("The Manacle", 14), The_Mark("The Mark", 15), The_Mouth("The Mouth", 16),
    The_Needle("The Needle", 17), The_Ox("The Ox", 18), The_Pillar("The Pillar", 19),
    The_Plant("The Plant", 20), The_Psychic("The Psychic", 21), The_Serpent("The Serpent", 22),
    The_Tooth("The Tooth", 23), The_Wall("The Wall", 24), The_Water("The Water", 25),
    The_Wheel("The Wheel", 26), The_Window("The Window", 27);
    override val y get() = 6
    val startsWithT get() = rawValue.startsWith("The ")
    companion object { fun allCases() = entries }
}

enum class Suit(override val rawValue: String, override val bitOrdinal: Int) : Item {
    Hearts("Hearts", 0), Clubs("Clubs", 1), Diamonds("Diamonds", 2), Spades("Spades", 3);
    override val y get() = -1
    fun index() = bitOrdinal
}

enum class Rank(override val rawValue: String) : Item {
    r_2("2"), r_3("3"), r_4("4"), r_5("5"), r_6("6"), r_7("7"), r_8("8"), r_9("9"),
    r_10("10"), Jack("J"), Queen("Q"), King("K"), Ace("A");
    override val bitOrdinal: Int get() = 0
    override val y get() = 0
    fun index() = when(this) { r_2->0; r_3->1; r_4->2; r_5->3; r_6->4; r_7->5; r_8->6; r_9->7; r_10->8; Jack->9; Queen->10; King->11; Ace->12 }
}
