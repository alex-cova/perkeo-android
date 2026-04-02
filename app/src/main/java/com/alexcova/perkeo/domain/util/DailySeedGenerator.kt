package com.alexcova.perkeo.domain.util

import java.security.MessageDigest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DailySeedGenerator {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private const val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    fun generate(date: LocalDate = LocalDate.now()): String {
        val dateString = date.format(dateFormatter)
        val bytes = MessageDigest.getInstance("SHA-256").digest(dateString.toByteArray())
        return buildString {
            repeat(8) { index ->
                val value = bytes[index].toInt() and 0xFF
                append(chars[value % chars.length])
            }
        }
    }
}

