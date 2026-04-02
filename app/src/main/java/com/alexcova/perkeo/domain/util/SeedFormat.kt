package com.alexcova.perkeo.domain.util

object SeedFormat {
    private val pattern = Regex("^[a-zA-Z0-9]{1,8}$")

    fun isValid(seed: String): Boolean = pattern.matches(seed)

    fun normalize(seed: String): String {
        return seed.uppercase().replace('0', 'O')
    }
}

