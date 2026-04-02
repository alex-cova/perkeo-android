package com.alexcova.perkeo.domain

import com.alexcova.perkeo.domain.util.AnalysisMath
import com.alexcova.perkeo.domain.util.LuaRandom
import com.alexcova.perkeo.domain.util.Seed32Bit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainParityTest {

    @Test
    fun luaRandom_isDeterministic() {
        val value = LuaRandom.random(0.0409823001603)
        val value2 = LuaRandom.random(0.0409823001603)
        assertEquals(value, value2, 0.0)
    }

    @Test
    fun pseudoHash_isDeterministic() {
        val hash1 = AnalysisMath.pseudoHash("hello")
        val hash2 = AnalysisMath.pseudoHash("hello")
        assertEquals(hash1, hash2, 0.0)
    }

    @Test
    fun seed32bit_encodeDecode_roundTrips() {
        val codec = Seed32Bit()
        val seed = codec.generateSeed()
        val encoded = codec.encode(seed)
        val decoded = codec.decode(encoded)
        assertEquals(seed, decoded)
    }

    @Test
    fun seedable_accepts8Or24Bits() {
        assertTrue(Seed32Bit.isSeedable(0x000000FF))
        assertTrue(Seed32Bit.isSeedable(-256))
    }
}

