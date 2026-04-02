package com.alexcova.perkeo.domain.util

import kotlin.random.Random

class Seed32Bit {
    companion object {
        private val CHARACTERS_ON = "L7H1VC549TMO83JSKBNER6GPDF2QUIWA".toCharArray()
        private val CHARACTERS_OFF = "N6F8ETHC4W5M7PQBSRAILDKUXG9ZYOVJ".toCharArray()

        fun isSeedable(value: Int): Boolean {
            val count = value.countOneBits()
            return count == 8 || count == 24
        }
    }

    fun generateSeed(): String = decode(generateIntSeed())

    fun generateIntSeed(): Int {
        val generateOff = Random.nextBoolean()
        return generateSeedWithBits(if (generateOff) 24 else 8, generateOff)
    }

    private fun generateSeedWithBits(bitCount: Int, off: Boolean): Int {
        var value = if (off) -1 else 0

        while (value.countOneBits() != bitCount) {
            val bit = 1 shl Random.nextInt(0, 32)
            value = if (off) value and bit.inv() else value or bit
        }

        return value
    }

    fun encode(seed: String): Int {
        var valueOn = 0
        var valueOff = -1
        var hasOffChar = false

        for (c in seed) {
            if (c > 'W') {
                hasOffChar = true
            }

            for (i in 0 until 32) {
                if (c == CHARACTERS_ON[i]) {
                    valueOn = valueOn or (1 shl i)
                }
                if (c == CHARACTERS_OFF[i]) {
                    valueOff = valueOff and (1 shl i).inv()
                }
            }
        }

        if (hasOffChar) {
            return validateAndReturn(valueOff, 24, seed)
        }

        if (valueOn.countOneBits() == 8 && seed == decode(valueOn)) {
            return valueOn
        }

        return validateAndReturn(valueOff, 24, seed)
    }

    private fun validateAndReturn(value: Int, expectedBits: Int, seed: String): Int {
        require(value.countOneBits() == expectedBits) {
            "Not a valid 32-bit seed: $seed"
        }
        return value
    }

    fun decode(value: Int): String {
        val bitCount = value.countOneBits()
        require(bitCount == 8 || bitCount == 24) {
            "Invalid seed bit count: $value"
        }

        val charset = if (bitCount == 8) CHARACTERS_ON else CHARACTERS_OFF
        val expected = if (bitCount == 8) 1 else 0
        val out = StringBuilder()

        for (i in 0 until 32) {
            if (out.length >= 8) {
                break
            }
            val bit = (value shr i) and 1
            if (bit == expected) {
                out.append(charset[i])
            }
        }

        return out.toString()
    }
}

