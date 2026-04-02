package com.alexcova.perkeo.features.analyzer

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alexcova.perkeo.domain.engine.JokerCount
import com.alexcova.perkeo.domain.engine.Run
import com.alexcova.perkeo.ui.components.AnimatedTitle
import com.alexcova.perkeo.ui.sprite.SpriteView
import com.alexcova.perkeo.ui.theme.*

@Composable
fun RunSummarySheet(run: Run) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .background(PerkeoBackgroundDark)
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedTitle(text = "Summary of ${run.seed}", modifier = Modifier.padding(bottom = 8.dp))

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            // Horizontal vouchers scroll
            val vouchers = run.vouchers()
            if (vouchers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (voucher in vouchers) {
                        SpriteView(item = voucher, showLabel = false, modifier = Modifier.size(71.dp, 95.dp))
                    }
                }
            }

            // Tags horizontal scroll
            val tags = run.tags()
            if (tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (tag in tags) {
                        SpriteView(item = tag, showLabel = false, modifier = Modifier.size(34.dp, 34.dp))
                    }
                }
            }

            // Jokers grid
            val jokers = run.jokers()
            if (jokers.isNotEmpty()) {
                JokerGrid(jokers = jokers)
            }

            // Spectrals grid
            val spectrals = run.spectrals()
            if (spectrals.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    for (spectral in spectrals) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            SpriteView(item = spectral, showLabel = true, modifier = Modifier.size(71.dp, 95.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun JokerGrid(jokers: List<JokerCount>) {
    val columns = 4
    val rows = (jokers.size + columns - 1) / columns
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (col in 0 until columns) {
                    val index = row * columns + col
                    if (index < jokers.size) {
                        val joker = jokers[index]
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f),
                        ) {
                            SpriteView(
                                item = joker.joker,
                                edition = joker.edition,
                                showLabel = true,
                                modifier = Modifier.size(71.dp, 95.dp),
                            )
                            if (joker.source.isNotEmpty()) {
                                Text(
                                    text = "At ante ${joker.ante} in ${joker.source}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                )
                            }
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
