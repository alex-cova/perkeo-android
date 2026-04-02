package com.alexcova.perkeo.domain.util

object RandomSeedGenerator {
    private const val chars = "123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    fun generate(length: Int = 8): String {
        return buildString {
            repeat(length) { append(chars.random()) }
        }
    }
}

