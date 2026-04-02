package com.alexcova.perkeo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.alexcova.perkeo.R

val GameFontFamily = FontFamily(Font(R.font.m6x11plus, FontWeight.Normal))

val Typography = Typography(
    displayLarge  = TextStyle(fontFamily = GameFontFamily, fontSize = 34.sp, fontWeight = FontWeight.Bold),
    displayMedium = TextStyle(fontFamily = GameFontFamily, fontSize = 28.sp, fontWeight = FontWeight.Bold),
    displaySmall  = TextStyle(fontFamily = GameFontFamily, fontSize = 24.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontFamily = GameFontFamily, fontSize = 24.sp, fontWeight = FontWeight.Bold),
    headlineMedium= TextStyle(fontFamily = GameFontFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontFamily = GameFontFamily, fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    titleLarge    = TextStyle(fontFamily = GameFontFamily, fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    titleMedium   = TextStyle(fontFamily = GameFontFamily, fontSize = 18.sp, fontWeight = FontWeight.Medium),
    titleSmall    = TextStyle(fontFamily = GameFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium),
    bodyLarge     = TextStyle(fontFamily = GameFontFamily, fontSize = 18.sp, fontWeight = FontWeight.Normal),
    bodyMedium    = TextStyle(fontFamily = GameFontFamily, fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodySmall     = TextStyle(fontFamily = GameFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal),
    labelLarge    = TextStyle(fontFamily = GameFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Medium),
    labelMedium   = TextStyle(fontFamily = GameFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal),
    labelSmall    = TextStyle(fontFamily = GameFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Normal),
)