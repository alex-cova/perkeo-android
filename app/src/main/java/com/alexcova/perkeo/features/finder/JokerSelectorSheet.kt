package com.alexcova.perkeo.features.finder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alexcova.perkeo.domain.engine.*
import com.alexcova.perkeo.ui.sprite.SpriteView
import com.alexcova.perkeo.ui.theme.PerkeoAccentRed
import com.alexcova.perkeo.ui.theme.PerkeoBackgroundDark
import com.alexcova.perkeo.ui.theme.PerkeoRowBackground
import com.alexcova.perkeo.ui.theme.PerkeoSurfaceDark
import com.alexcova.perkeo.ui.theme.PerkeoTextMuted
import java.util.UUID

@Composable
fun JokerSelectorSheet(
    selections: List<DraggableItem>,
    onAdd: (DraggableItem) -> Unit,
    onRemove: (String) -> Unit,
) {
    var search by remember { mutableStateOf("") }
    var showSelected by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .background(PerkeoBackgroundDark),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Joker Selection",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
            val countColor = if (selections.size == 10) PerkeoAccentRed else Color.White
            Text(
                "${selections.size} of 10",
                style = MaterialTheme.typography.labelMedium,
                color = countColor,
            )
        }

        // Search + filter toggle
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search", color = Color.Gray) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = PerkeoSurfaceDark,
                    unfocusedContainerColor = PerkeoSurfaceDark,
                    focusedBorderColor = PerkeoAccentRed,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                ),
            )
            IconButton(
                onClick = { showSelected = !showSelected },
                modifier = Modifier.background(
                    if (showSelected) PerkeoAccentRed else PerkeoBackgroundDark,
                    MaterialTheme.shapes.small,
                ),
            ) {
                Icon(Icons.Default.Check, null, tint = Color.White)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = Color.DarkGray)

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        ) {
            if (showSelected) {
                if (selections.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Nothing selected", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                    }
                } else {
                    ItemSection(
                        title = "Selections",
                        items = selections.map { it.toItemInfo() },
                        selections = selections,
                        onAdd = onAdd,
                        onRemove = onRemove,
                        search = "",
                    )
                }
            } else {
                ItemSection("Legendary", LegendaryJoker.allCases().map { ItemInfo("LegendaryJoker", it.rawValue, it) }, selections, onAdd, onRemove, search)
                ItemSection("Vouchers", Voucher.allCases().map { ItemInfo("Voucher", it.rawValue, it) }, selections, onAdd, onRemove, search)
                ItemSection("Rare Jokers", RareJoker.allCases().map { ItemInfo("RareJoker", it.rawValue, it) }, selections, onAdd, onRemove, search)
                ItemSection("Uncommon", UnCommonJoker.allCases().map { ItemInfo("UnCommonJoker", it.rawValue, it) }, selections, onAdd, onRemove, search)
                ItemSection("Common", CommonJoker.allCases().map { ItemInfo("CommonJoker", it.rawValue, it) }, selections, onAdd, onRemove, search)
                ItemSection("Spectrals", Spectral.allCases().map { ItemInfo("Spectral", it.rawValue, it) }, selections, onAdd, onRemove, search)
            }
        }
    }
}

private data class ItemInfo(val kind: String, val name: String, val item: Item)

private fun DraggableItem.toItemInfo(): ItemInfo {
    val item: Item = try { this.item() } catch (_: Exception) { return ItemInfo(kind, name, CommonJoker.Joker) }
    return ItemInfo(kind, name, item)
}

@Composable
private fun ItemSection(
    title: String,
    items: List<ItemInfo>,
    selections: List<DraggableItem>,
    onAdd: (DraggableItem) -> Unit,
    onRemove: (String) -> Unit,
    search: String,
) {
    val filtered = remember(search, items) { if (search.isEmpty()) items else items.filter { matchesSearch(it.name, search) } }
    if (filtered.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = PerkeoTextMuted,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(filtered, key = { it.name }) { info ->
                val selected = selections.any { it.name == info.name }
                val isLegendary = info.kind == "LegendaryJoker"
                Box(
                    modifier = Modifier
                        .alpha(if (selected) 0.3f else 1f)
                        .clickable {
                            if (selected) {
                                val existing = selections.find { it.name == info.name }
                                if (existing != null) onRemove(existing.id)
                            } else if (selections.size < 10) {
                                onAdd(
                                    DraggableItem(
                                        kind = info.kind,
                                        name = info.name,
                                        edition = Edition.NoEdition,
                                        score = 1.0,
                                        id = UUID.randomUUID().toString(),
                                    )
                                )
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    val spriteItem: Item = if (isLegendary) {
                        LegendaryJoker.entries.find { it.rawValue == info.name } ?: info.item
                    } else {
                        info.item
                    }
                    val size = if (isLegendary) Modifier.size(71.dp, 95.dp) else Modifier.size(71.dp, 95.dp)
                    SpriteView(item = spriteItem, showLabel = false, modifier = size)
                    if (selected) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

private fun matchesSearch(name: String, query: String): Boolean {
    val q = query.lowercase()
    val n = name.lowercase()
    if (n.contains(q)) return true
    return levenshtein(n, q) < 2
}

private fun levenshtein(a: String, b: String): Int {
    val aArr = a.toCharArray()
    val bArr = b.toCharArray()
    var last = IntArray(bArr.size + 1) { it }
    for ((i, aC) in aArr.withIndex()) {
        var current = IntArray(bArr.size + 1)
        current[0] = i + 1
        for ((j, bC) in bArr.withIndex()) {
            current[j + 1] = if (aC == bC) last[j] else minOf(last[j], last[j + 1], current[j]) + 1
        }
        last = current
    }
    return last[bArr.size]
}
