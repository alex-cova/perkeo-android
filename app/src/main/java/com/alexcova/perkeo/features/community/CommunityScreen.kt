package com.alexcova.perkeo.features.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alexcova.perkeo.domain.engine.BalatroAnalyzer
import com.alexcova.perkeo.features.analyzer.AnalyzerViewModel
import com.alexcova.perkeo.ui.components.AnimatedTitle
import com.alexcova.perkeo.ui.theme.PerkeoBackgroundDark
import com.alexcova.perkeo.ui.theme.PerkeoTextMuted

@Composable
fun CommunityScreen(
    analyzerViewModel: AnalyzerViewModel,
    onNavigateToSeed: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var seeds by remember { mutableStateOf(generateSeeds()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PerkeoBackgroundDark),
    ) {
        AnimatedTitle(text = "Community Seeds")

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize().background(PerkeoBackgroundDark),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(seeds, key = { it }) { seed ->
                CommunitySeedView(seed = seed, onClick = { onNavigateToSeed(seed) })
            }
            item {
                // Credits at the end
                Text(
                    text = "Thanks to LocalThunk for the amazing game, to the people at Balatro discord server, to math, tacodiva, pifreak, saul and other friends of the community for their help and support!",
                    style = MaterialTheme.typography.labelSmall,
                    color = PerkeoTextMuted,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    }
}

@Composable
private fun CommunitySeedView(seed: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(120.dp, 100.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(5.dp, Color.Black, RoundedCornerShape(8.dp))
            .background(PerkeoBackgroundDark)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = seed,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
        )
    }
}

private fun generateSeeds(): List<String> = List(80) { BalatroAnalyzer.generateRandomString() }
