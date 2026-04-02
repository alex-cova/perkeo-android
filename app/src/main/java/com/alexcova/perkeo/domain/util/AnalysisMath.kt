package com.alexcova.perkeo.domain.util

import kotlin.math.floor
import kotlin.math.nextUp
import kotlin.math.pow

object AnalysisMath {
    private val invPrecision = 10.0.pow(13)
    private val twoInvPrecision = 2.0.pow(13)
    private val fiveInvPrecision = 5.0.pow(13)

    fun fraction(value: Double): Double = value - floor(value)

    fun round13(value: Double): Double {
        val tentative = floor(value * invPrecision) / invPrecision
        val truncated = ((value * twoInvPrecision) % 1.0) * fiveInvPrecision

        if (tentative != value && tentative != value.nextUp() && truncated % 1.0 >= 0.5) {
            return (floor(value * invPrecision) + 1) / invPrecision
        }

        return tentative
    }

    fun pseudoHash(source: String): Double {
        var number = 1.0

        for (i in source.indices.reversed()) {
            val c = source[i].code.toDouble()
            number = fraction(
                (1.1239285023 / number) * c * Math.PI + Math.PI * (i + 1),
            )
        }

        return if (number.isNaN()) Double.NaN else number
    }
}

