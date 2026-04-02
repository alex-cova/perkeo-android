package com.alexcova.perkeo.domain.model.finder

data class CompressedSeed(
    val memory: LongArray,
) {
    fun getBit(index: Int): Boolean {
        val word = index / 64
        val bit = index % 64
        return (memory[word] and (1L shl bit)) != 0L
    }
}

