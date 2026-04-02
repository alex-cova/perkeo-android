package com.alexcova.perkeo.domain.engine

object RunScorer {
    fun score(run: Run): Int {
        var score = 0f
        for (ante in run.antes) score += calculateScore(ante)

        if (run.hasLegendary(LegendaryJoker.Perkeo)) {
            if (run.hasVoucher(Voucher.Observatory)) score += 25f
            if (run.hasJoker(RareJoker.Blueprint, 10)) score += 25f
            if (run.hasJoker(RareJoker.Brainstorm, 10)) score += 25f
            if (run.hasJoker(RareJoker.Baron, 10) && run.hasJoker(UnCommonJoker.Mime, 10)) score += 15f
        }
        if (run.hasLegendary(LegendaryJoker.Triboulet)) {
            if (run.hasJoker(RareJoker.Blueprint, 10)) score += 25f
            if (run.hasJoker(RareJoker.Brainstorm, 10)) score += 25f
            if (run.hasJoker(UnCommonJoker.Sock_and_Buskin, 10)) score += 25f
        }
        if (run.hasLegendary(LegendaryJoker.Canio)) {
            if (run.hasJoker(UnCommonJoker.Pareidolia, 10)) score += 25f
        }
        return score.toInt()
    }

    private fun calculateScore(ante: Ante): Float {
        var score = 0f
        for (value in ante.legendaries ?: emptyList()) {
            score = 50f * value.type.rarity * value.edition.multiplier
        }
        for ((i, item) in ante.shopQueue.withIndex()) {
            val joker = item.item as? Joker
            if (joker != null) score += (50f - i) * joker.jokerType.rarity * (item.edition?.multiplier ?: 0f)
            if (i > 30) continue
            if (item.item is Spectral) {
                if (item.equals(Spectral.Cryptid)) score += 2f
                if (item.equals(Spectral.Deja_Vu)) score += 4f
            }
            if (item.item is Tarot) {
                if (item.equals(Tarot.Temperance)) score += 2f
                if (item.equals(Tarot.The_Hermit)) score += 1.5f
                if (item.equals(Tarot.The_Fool)) score += 1f
            }
        }
        for (pack in ante.packs) {
            if (pack.kind == PackKind.Standard) continue
            score += pack.choices
            if (pack.kind == PackKind.Spectral) {
                if (pack.containsOption(Spectral.Cryptid.rawValue)) score += 2f
                if (pack.containsOption(Spectral.Deja_Vu.rawValue)) score += 4f
            }
            if (pack.kind == PackKind.Arcana) {
                if (pack.containsOption(Tarot.Temperance.rawValue)) score += 2f
                if (pack.containsOption(Tarot.The_Hermit.rawValue)) score += 1.5f
                if (pack.containsOption(Tarot.The_Fool.rawValue)) score += 1f
            }
            if (pack.kind == PackKind.Buffoon) {
                for (opt in pack.options) {
                    val j = opt.item as? Joker ?: continue
                    score += 50f * j.jokerType.rarity * opt.edition.multiplier
                }
            }
        }
        for (tag in ante.tags) {
            when (tag) { Tag.Negative_Tag -> score += 5f; Tag.Charm_Tag -> score += 0.5f; else -> {} }
        }
        if (ante.boss == Boss.The_Arm) score -= 0.5f
        if (ante.countInPack(Specials.BLACKHOLE) > 0) score += 5f
        when (ante.voucher) {
            Voucher.Blank -> score += 1f; Voucher.Clearance_Sale -> score += 0.5f; Voucher.Overstock -> score += 0.2f
            Voucher.Liquidation -> score += 0.5f; Voucher.Hieroglyph -> score += 0.5f; Voucher.Paint_Brush -> score += 0.5f
            Voucher.Recyclomancy -> score += 0.5f; Voucher.Grabber -> score += 0.5f; Voucher.Wasteful -> score += 0.2f
            else -> {}
        }
        score += maxOf(0, 8 - ante.ante) * 10f
        return score
    }
}

