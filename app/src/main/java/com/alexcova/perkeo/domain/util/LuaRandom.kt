package com.alexcova.perkeo.domain.util

object LuaRandom {
    private const val MAX_UINT64 = ULong.MAX_VALUE

    private fun randInt(seedValue: Double): ULong {
        var seed = seedValue
        var randomInt = 0UL
        var r = 0x11090601

        fun scramble(
            shiftA: Int,
            shiftB: Int,
            maskShift: Int,
            postShift: Int,
            rounds: Int,
        ): ULong {
            var localSeed = seed
            var localR = r
            var m = 1UL shl (localR and 255)
            localR = localR shr 8
            localSeed = localSeed * Math.PI + Math.E
            var bits = localSeed.toBits().toULong()
            if (bits < m) {
                bits += m
            }

            var state = bits
            repeat(rounds) {
                state = (((state shl shiftA) xor state) shr shiftB) xor
                    ((state and (MAX_UINT64 shl maskShift)) shl postShift)
                state = (((state shl shiftA) xor state) shr shiftB) xor
                    ((state and (MAX_UINT64 shl maskShift)) shl postShift)
            }

            state = (((state shl shiftA) xor state) shr shiftB) xor
                ((state and (MAX_UINT64 shl maskShift)) shl postShift)

            seed = localSeed
            r = localR
            return state
        }

        randomInt = randomInt xor scramble(31, 45, 1, 18, 5)
        randomInt = randomInt xor scramble(19, 30, 6, 28, 5)
        randomInt = randomInt xor scramble(24, 48, 9, 7, 5)
        randomInt = randomInt xor scramble(21, 39, 17, 8, 5)

        return randomInt
    }

    private fun randDoubleMemory(seed: Double): ULong {
        return (randInt(seed) and 4_503_599_627_370_495UL) or 4_607_182_418_800_017_408UL
    }

    fun random(seed: Double): Double {
        return Double.fromBits(randDoubleMemory(seed).toLong()) - 1.0
    }

    fun randomInt(seed: Double, min: Int, max: Int): Int {
        return (random(seed) * (max - min + 1)).toInt() + min
    }
}

