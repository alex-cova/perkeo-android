package com.alexcova.perkeo.domain.engine

import java.util.UUID

data class DraggableItem(
    val kind: String,
    val name: String,
    val edition: Edition,
    val score: Double,
    val id: String = UUID.randomUUID().toString(),
) {
    fun nextEdition() = copy(edition = edition.next)

    fun item(): Item {
        return when (kind) {
            "LegendaryJoker" -> LegendaryJoker.entries.find { it.rawValue == name }?.let { EditionItem(edition, it) }
            "RareJoker" -> RareJoker.entries.find { it.rawValue == name }?.let { EditionItem(edition, it) }
            "UnCommonJoker" -> UnCommonJoker.entries.find { it.rawValue == name }?.let { EditionItem(edition, it) }
            "CommonJoker" -> CommonJoker.entries.find { it.rawValue == name }?.let { EditionItem(edition, it) }
            "Spectral" -> Spectral.entries.find { it.rawValue == name }?.let { EditionItem(edition, it) }
            "Voucher" -> Voucher.entries.find { it.rawValue == name }?.let { EditionItem(Edition.NoEdition, it) }
            else -> null
        } ?: error("Unknown kind '$kind' or name '$name'")
    }
}

enum class ClauseType { MUST, SHOULD, MUST_NOT }

