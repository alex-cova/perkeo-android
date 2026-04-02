package com.alexcova.perkeo.domain.engine

import kotlin.math.floor

object Util {
    private val invPrec = Math.pow(10.0, 13.0)
    private val twoInvPrec = Math.pow(2.0, 13.0)
    private val fiveInvPrec = Math.pow(5.0, 13.0)

    fun fract(n: Double) = n - floor(n)

    fun round13(x: Double): Double {
        val tentative = floor(x * invPrec) / invPrec
        val truncated = ((x * twoInvPrec) % 1.0) * fiveInvPrec
        return if (tentative != x && tentative != Math.nextUp(x) && truncated % 1.0 >= 0.5)
            (floor(x * invPrec) + 1) / invPrec else tentative
    }

    fun pseudohash(s: String): Double {
        var num = 1.0
        for (i in s.indices.reversed()) {
            val c = s[i].code.toDouble()
            num = fract(1.1239285023 / num * c * Math.PI + Math.PI * (i + 1))
        }
        return if (num.isNaN()) Double.NaN else num
    }
}

