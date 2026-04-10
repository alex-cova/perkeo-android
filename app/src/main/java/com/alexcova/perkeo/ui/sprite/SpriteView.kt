package com.alexcova.perkeo.ui.sprite

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexcova.perkeo.domain.engine.*
import com.alexcova.perkeo.ui.theme.GameFontFamily
import androidx.core.graphics.createBitmap

@Composable
fun SpriteView(
    item: Item,
    edition: Edition? = null,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    size: Dp = 71.dp,
) {
    val effectiveItem = if (item is EditionItem) item.item else item
    val effectiveEdition = edition ?: (item as? EditionItem)?.edition

    val card = effectiveItem as? Card
    val isCard = card != null
    val isLegendary = effectiveItem is LegendaryJoker
    val isAnimated = isLegendary || effectiveItem.rawValue == "Hologram"

    val infiniteTransition = rememberInfiniteTransition(label = "sprite")
    val angle by if (isAnimated) infiniteTransition.animateFloat(
        initialValue = -2f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1500, easing = EaseInOut), RepeatMode.Reverse),
        label = "rotation"
    ) else remember { mutableFloatStateOf(0f) }

    val spriteInfo = remember(effectiveItem.rawValue) { SpriteSheets.getSpriteInfo(effectiveItem, effectiveEdition) }
    val (mainBitmap, mainImageBitmap) = remember(spriteInfo) {
        val bmp = SpriteSheets.cropBitmap(spriteInfo.bitmap, spriteInfo.pos, spriteInfo.cellW, spriteInfo.cellH)
        bmp to bmp?.asImageBitmap()
    }
    val editionImageBitmap = remember(effectiveEdition) { effectiveEdition?.let { SpriteSheets.getEditionBitmap(it, spriteInfo.cellW, spriteInfo.cellH)?.asImageBitmap() } }

    val isNegative = effectiveEdition == Edition.Negative

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        val w = size; val h = size * (spriteInfo.cellH.toFloat() / spriteInfo.cellW)
        Box(modifier = Modifier.size(w, h), contentAlignment = Alignment.Center) {
            if (card != null) {
                // Layer 1: enhancement background
                val enhImageBitmap = remember(card.enhancement) { SpriteSheets.getEnhancementBitmap(card.enhancement)?.asImageBitmap() }
                if (enhImageBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = enhImageBitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // Layer 2: card face (skip for Stone)
                if (card.enhancement != Enhancement.Stone && mainImageBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = mainImageBitmap,
                        contentDescription = effectiveItem.rawValue,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // Layer 3: edition overlay (use card's own edition)
                val cardEdition = card.edition
                if (cardEdition != Edition.NoEdition) {
                    val cardEdImageBitmap = remember(cardEdition) { SpriteSheets.getEditionBitmap(cardEdition)?.asImageBitmap() }
                    if (cardEdImageBitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = cardEdImageBitmap,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                // Layer 4: seal overlay
                val cardSeal = card.seal
                if (cardSeal != Seal.NoSeal) {
                    val sealImageBitmap = remember(cardSeal) { SpriteSheets.getSealBitmap(cardSeal)?.asImageBitmap() }
                    if (sealImageBitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = sealImageBitmap,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            } else if (mainBitmap != null) {
                val imageBitmap = remember(mainBitmap, isNegative) {
                    if (isNegative) mainBitmap.invertColors().asImageBitmap() else mainBitmap.asImageBitmap()
                }
                androidx.compose.foundation.Image(
                    bitmap = imageBitmap,
                    contentDescription = effectiveItem.rawValue,
                    modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = angle }
                )
                if (editionImageBitmap != null && !isNegative) {
                    androidx.compose.foundation.Image(
                        bitmap = editionImageBitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        if (showLabel) {
            Text(
                text = effectiveItem.rawValue,
                fontFamily = GameFontFamily,
                fontSize = 9.sp,
                color = androidx.compose.ui.graphics.Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 10.sp,
                modifier = Modifier.widthIn(max = size)
            )
            if (effectiveEdition != null && effectiveEdition != Edition.NoEdition) {
                Text(
                    text = effectiveEdition.rawValue,
                    fontFamily = GameFontFamily,
                    fontSize = 8.sp,
                    color = androidx.compose.ui.graphics.Color.Red,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private fun Bitmap.invertColors(): Bitmap {
    val result = createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(result)
    val paint = Paint().apply { colorFilter = PorterDuffColorFilter(0x00FFFFFF, PorterDuff.Mode.XOR) }
    canvas.drawBitmap(this, 0f, 0f, paint)
    return result
}

