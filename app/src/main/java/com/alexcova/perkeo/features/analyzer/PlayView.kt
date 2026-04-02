package com.alexcova.perkeo.features.analyzer

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alexcova.perkeo.domain.engine.*
import com.alexcova.perkeo.ui.sprite.SpriteView
import com.alexcova.perkeo.ui.theme.*

@Composable
fun PlayView(run: Run, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.background(PerkeoBackgroundDark), contentPadding = PaddingValues(bottom = 80.dp)) {
        if (run.antes.size >= 8) {
            item {
                Text("Seed score: ${run.score}", color = Color.White, style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }
        }
        items(run.antes) { ante -> AnteView(ante = ante, run = run) }
    }
}

@Composable
private fun AnteView(ante: Ante, run: Run) {
    Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
        Text("Ante ${ante.ante}", style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        GameDivider()
        AnteOptions(ante = ante)
        Text("Shop queue", style = MaterialTheme.typography.bodyLarge, color = Color.White, modifier = Modifier.padding(top = 8.dp))
        GameDivider()
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            for (item in ante.shopQueue) {
                SpriteView(item = item.asEditionItem(), modifier = Modifier.padding(4.dp))
            }
        }
        for (pack in ante.packs) PackView(pack = pack, ante = ante)
    }
}

@Composable
private fun AnteOptions(ante: Ante) {
    Row(modifier = Modifier.padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        SpriteView(item = ante.voucher, modifier = Modifier.padding(horizontal = 8.dp))
        Row {
            SpriteView(item = ante.boss)
            for (tag in ante.tags) SpriteView(item = tag)
        }
    }
}

@Composable
private fun PackView(pack: Pack, ante: Ante) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(pack.type.rawValue, style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Surface(shape = MaterialTheme.shapes.extraSmall, color = Color(0xFF1565C0)) {
                Text(choiceText(pack.choices), style = MaterialTheme.typography.labelMedium, color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
            }
        }
        GameDivider()
        if (pack.options.size > 4) {
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                for (opt in pack.options) SpriteView(item = opt, modifier = Modifier.padding(4.dp))
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (opt in pack.options) SpriteView(item = opt)
            }
        }
    }
}

@Composable
fun GameDivider() {
    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = PerkeoRowBackground, thickness = 1.dp)
}

private fun choiceText(choices: Int) = if (choices == 1) "Choose 1" else "$choices choices"

