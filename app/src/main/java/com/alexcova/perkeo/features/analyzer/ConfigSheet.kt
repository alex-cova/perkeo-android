package com.alexcova.perkeo.features.analyzer

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alexcova.perkeo.domain.engine.*
import com.alexcova.perkeo.ui.sprite.SpriteView
import com.alexcova.perkeo.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigSheet(
    state: AnalyzerUiState,
    viewModel: AnalyzerViewModel,
    context: Context,
) {
    val rowBg = PerkeoRowBackground
    val sectionBg = PerkeoSurfaceDark

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .background(PerkeoBackgroundDark)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        // ── Section 1: Seed actions + ante steppers + deck/stake ──
        ConfigSection(bg = sectionBg) {
            // Paste seed (only when relevant)
            ConfigRow(bg = rowBg) {
                TextButton(onClick = { viewModel.paste(context) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.ContentPaste, null, tint = PerkeoAccentRed)
                    Spacer(Modifier.width(8.dp))
                    Text("Paste Seed", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                }
            }
            ConfigRow(bg = rowBg) {
                TextButton(onClick = { viewModel.copy(context) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.ContentCopy, null, tint = PerkeoAccentRed)
                    Spacer(Modifier.width(8.dp))
                    Text("Copy Seed", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                }
            }
            ConfigRow(bg = rowBg) {
                TextButton(onClick = { viewModel.storeSeed() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Download, null, tint = PerkeoAccentRed)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Seed", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                }
            }

            // Starting ante stepper
            ConfigRow(bg = rowBg) {
                AnteStepperRow(
                    label = "starting ante: ${state.startingAnte}",
                    icon = Icons.Default.ArrowForward,
                    iconTint = PerkeoAccentRed,
                    onIncrement = {
                        val next = (state.startingAnte + 1).coerceAtMost(29)
                        viewModel.setStartingAnte(next)
                        if (next > state.maxAnte) viewModel.setMaxAnte(next)
                    },
                    onDecrement = {
                        viewModel.setStartingAnte((state.startingAnte - 1).coerceAtLeast(1))
                    },
                )
            }

            // Max ante stepper
            ConfigRow(bg = rowBg) {
                AnteStepperRow(
                    label = "max ante: ${state.maxAnte}",
                    icon = Icons.Default.Warning,
                    iconTint = Color(0xFFFFD600),
                    onIncrement = { viewModel.setMaxAnte((state.maxAnte + 1).coerceAtMost(30)) },
                    onDecrement = {
                        val next = (state.maxAnte - 1).coerceAtLeast(1)
                        viewModel.setMaxAnte(next)
                        if (next < state.startingAnte) viewModel.setStartingAnte(next.coerceAtLeast(1))
                    },
                    subtitle = "Deeper ante search is slower!",
                )
            }

            // Showman toggle
            ConfigRow(bg = rowBg) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Showman", style = MaterialTheme.typography.bodyLarge, color = Color.White,
                        modifier = Modifier.weight(1f))
                    Switch(checked = state.showman, onCheckedChange = viewModel::setShowman,
                        colors = SwitchDefaults.colors(checkedThumbColor = PerkeoAccentRed,
                            checkedTrackColor = PerkeoAccentRed.copy(alpha = 0.5f)))
                }
            }

            // Deck picker
            ConfigRow(bg = rowBg) { DeckPicker(state = state, onDeckSelected = viewModel::setDeck) }

            // Stake picker
            ConfigRow(bg = rowBg) { StakePicker(state = state, onStakeSelected = viewModel::setStake) }
        }

        // ── Section 2: Vouchers ──
        ConfigSection(bg = sectionBg) {
            ConfigRow(bg = rowBg) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Auto buy vouchers", style = MaterialTheme.typography.bodyLarge, color = Color.White,
                        modifier = Modifier.weight(1f))
                    Switch(checked = state.autoBuyVoucher, onCheckedChange = viewModel::setAutoBuyVoucher,
                        colors = SwitchDefaults.colors(checkedThumbColor = PerkeoAccentRed,
                            checkedTrackColor = PerkeoAccentRed.copy(alpha = 0.5f)))
                }
            }
            if (!state.autoBuyVoucher) {
                ConfigRow(bg = rowBg) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CheckBox, null, tint = Color.Gray)
                        Text("Select the vouchers you have purchased",
                            style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                    }
                }
                JokerDisclosureGroup(
                    title = "Vouchers",
                    items = Voucher.allCases(),
                    isSelected = { viewModel.isSelected(it) },
                    onToggle = { viewModel.toggleDisabledItem(it) },
                    invertOpacity = true,
                    bg = rowBg,
                )
            }
        }

        // ── Section 3: Disabled jokers ──
        ConfigSection(bg = sectionBg) {
            ConfigRow(bg = rowBg) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Cancel, null, tint = Color.Gray)
                    Text("Select the jokers you have already purchased",
                        style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                }
            }
            JokerDisclosureGroup(title = "Legendary Jokers", items = LegendaryJoker.allCases(),
                isSelected = { viewModel.isSelected(it) }, onToggle = { viewModel.toggleDisabledItem(it) }, bg = rowBg)
            JokerDisclosureGroup(title = "Rare Jokers", items = RareJoker.allCases(),
                isSelected = { viewModel.isSelected(it) }, onToggle = { viewModel.toggleDisabledItem(it) }, bg = rowBg)
            JokerDisclosureGroup(title = "Uncommon Jokers", items = UnCommonJoker.allCases(),
                isSelected = { viewModel.isSelected(it) }, onToggle = { viewModel.toggleDisabledItem(it) }, bg = rowBg)
            JokerDisclosureGroup(title = "Common Jokers", items = CommonJoker.allCases(),
                isSelected = { viewModel.isSelected(it) }, onToggle = { viewModel.toggleDisabledItem(it) }, bg = rowBg)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ConfigSection(bg: Color, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(bg).padding(vertical = 1.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
        content = content,
    )
}

@Composable
private fun ConfigRow(bg: Color, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(bg), content = { content() })
}

@Composable
private fun AnteStepperRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    subtitle: String? = null,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = iconTint)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Bold)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.labelMedium, color = Color.White)
        }
        Row {
            IconButton(onClick = onDecrement) { Icon(Icons.Default.Remove, null, tint = Color.White) }
            IconButton(onClick = onIncrement) { Icon(Icons.Default.Add, null, tint = Color.White) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeckPicker(state: AnalyzerUiState, onDeckSelected: (Deck) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        OutlinedTextField(
            value = state.deck.rawValue,
            onValueChange = {},
            readOnly = true,
            label = { Text("Deck", color = Color.White) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White,
                unfocusedTextColor = Color.White, focusedContainerColor = PerkeoSurfaceDark,
                unfocusedContainerColor = PerkeoSurfaceDark),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
            modifier = Modifier.background(PerkeoSurfaceDark)) {
            Deck.allCases().forEach { deck ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(deck.rawValue, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                            SpriteView(item = deck, showLabel = false, modifier = Modifier.size(35.dp, 47.dp))
                        }
                    },
                    onClick = { onDeckSelected(deck); expanded = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StakePicker(state: AnalyzerUiState, onStakeSelected: (Stake) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        OutlinedTextField(
            value = state.stake.rawValue,
            onValueChange = {},
            readOnly = true,
            label = { Text("Stake", color = Color.White) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White,
                unfocusedTextColor = Color.White, focusedContainerColor = PerkeoSurfaceDark,
                unfocusedContainerColor = PerkeoSurfaceDark),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
            modifier = Modifier.background(PerkeoSurfaceDark)) {
            Stake.entries.forEach { stake ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SpriteView(item = stake, showLabel = false, modifier = Modifier.size(29.dp, 29.dp))
                            Text(stake.rawValue, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        }
                    },
                    onClick = { onStakeSelected(stake); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun <T : Item> JokerDisclosureGroup(
    title: String,
    items: List<T>,
    isSelected: (T) -> Boolean,
    onToggle: (T) -> Unit,
    invertOpacity: Boolean = false,
    bg: Color,
) {
    var expanded by remember { mutableStateOf(false) }
    ConfigRow(bg = bg) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = Color.White,
                    modifier = Modifier.weight(1f))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = PerkeoAccentRed,
                )
            }
            if (expanded) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp).padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(items, key = { it.rawValue }) { item ->
                        val selected = isSelected(item)
                        val opacity = if (invertOpacity) {
                            if (selected) 1.0f else 0.3f
                        } else {
                            if (selected) 0.3f else 1.0f
                        }
                        Box(modifier = Modifier.clickable { onToggle(item) }.alpha(opacity)) {
                            SpriteView(
                                item = item,
                                showLabel = false,
                                modifier = Modifier.size(71.dp, 95.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
