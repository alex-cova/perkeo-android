package com.alexcova.perkeo.features.finder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alexcova.perkeo.domain.engine.*
import com.alexcova.perkeo.features.analyzer.AnalyzerViewModel
import com.alexcova.perkeo.ui.components.AnimatedTitle
import com.alexcova.perkeo.ui.components.TribouleteView
import com.alexcova.perkeo.ui.sprite.SpriteView
import com.alexcova.perkeo.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinderScreen(
    viewModel: FinderViewModel,
    analyzerViewModel: AnalyzerViewModel,
    onNavigateToSeed: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    var showJokerSelector by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(PerkeoBackgroundDark)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AnimatedTitle(text = "Seed Finder")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(PerkeoBackgroundDark),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // ── Controls section ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Heavy search (only when not in cache mode)
                    if (!state.useLegendarySearch && !state.useInstantSearch) {
                        HeavySearchSection(state = state, viewModel = viewModel)
                    }

                    // Legendary search toggle
                    LegendarySection(state = state, viewModel = viewModel)

                    // Instant search toggle
                    InstantSection(state = state, viewModel = viewModel)

                    if (state.loadingCache) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }

                HorizontalDivider(color = PerkeoSurfaceDark)

                // ── Joker selection area ──
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(onClick = { showJokerSelector = true }) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Circle, null, tint = PerkeoAccentRed)
                            Text("Select Jokers", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                        }
                    }

                    if (!state.isEmpty) {
                        ClauseZoneView(
                            title = "MUST (Required)",
                            icon = Icons.Default.CheckCircle,
                            color = Color(0xFF4CAF50),
                            clauses = state.must,
                            onRemove = viewModel::removeClause,
                            onMove = { id -> viewModel.addToClause(state.must.first { it.id == id }, ClauseType.SHOULD) },
                            onMoveLabel = "Move to SHOULD",
                        )
                        ClauseZoneView(
                            title = "SHOULD (Bonus Score)",
                            icon = Icons.Default.Star,
                            color = Color(0xFFFFD600),
                            clauses = state.should,
                            onRemove = viewModel::removeClause,
                            onMove = { id -> viewModel.addToClause(state.should.first { it.id == id }, ClauseType.MUST_NOT) },
                            onMoveLabel = "Move to MUST NOT",
                        )
                        ClauseZoneView(
                            title = "MUST NOT (Banned)",
                            icon = Icons.Default.Cancel,
                            color = PerkeoAccentRed,
                            clauses = state.mustNot,
                            onRemove = viewModel::removeClause,
                            onMove = { id -> viewModel.addToClause(state.mustNot.first { it.id == id }, ClauseType.MUST) },
                            onMoveLabel = "Move to MUST",
                        )

                        Spacer(Modifier.height(8.dp))
                    }

                    // Clear + Search buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (!state.foundSeeds.isEmpty() || !state.isEmpty) {
                            TextButton(onClick = viewModel::clearAll) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Close, null, tint = PerkeoAccentRed)
                                    Text("Clear selections", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                                }
                            }
                        }
                        if (!state.isEmpty) {
                            TextButton(onClick = viewModel::startSearch) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Search, null, tint = Color(0xFF4CAF50))
                                    Text("Search", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // ── Found seeds ──
                if (state.foundSeeds.isNotEmpty()) {
                    FoundSeedsSection(
                        state = state,
                        viewModel = viewModel,
                        onNavigate = onNavigateToSeed,
                    )
                } else {
                    Spacer(Modifier.height(100.dp))
                }
            }
        }

        // ── Search progress overlay ──
        if (state.searching) {
            SearchProgressSheet(
                state = state,
                onStop = viewModel::stopSearch,
            )
        }
    }

    // ── Joker selector bottom sheet ──
    if (showJokerSelector) {
        ModalBottomSheet(
            onDismissRequest = { showJokerSelector = false },
            containerColor = PerkeoRowBackground,
            scrimColor = Color.Black.copy(alpha = 0.5f),
        ) {
            JokerSelectorSheet(
                selections = state.must,
                onAdd = { item -> viewModel.addToClause(item, ClauseType.MUST) },
                onRemove = viewModel::removeClause,
            )
        }
    }
}

@Composable
private fun HeavySearchSection(state: FinderUiState, viewModel: FinderViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(PerkeoSurfaceDark)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Toggle header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.toggleHeavyControls() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Heavy Search",
                style = MaterialTheme.typography.bodyLarge,
                color = PerkeoAccentRed,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            val rotation by animateFloatAsState(targetValue = if (state.showHeavyControls) 180f else 0f)
            Icon(Icons.Default.ExpandMore, null, tint = PerkeoAccentRed, modifier = Modifier.rotate(rotation))
        }

        AnimatedVisibility(visible = state.showHeavyControls) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Seeds to analyze stepper
                StepperRow(
                    label = "Seeds to analyze",
                    value = "${state.seedsToAnalyze}",
                    onIncrement = viewModel::incrementSeedsToAnalyze,
                    onDecrement = viewModel::decrementSeedsToAnalyze,
                )
                // Starting ante
                StepperRow(
                    label = "starting ante: ${state.startingAnte}",
                    value = null,
                    iconTint = PerkeoAccentRed,
                    onIncrement = viewModel::incrementStartingAnte,
                    onDecrement = viewModel::decrementStartingAnte,
                )
                // Last ante
                StepperRow(
                    label = "last ante: ${state.maxAnte}",
                    value = null,
                    iconTint = if (state.isHeavySearch) Color(0xFFFFD600) else PerkeoAccentRed,
                    subtitle = if (state.isHeavySearch) "This might take a while" else null,
                    onIncrement = viewModel::incrementMaxAnte,
                    onDecrement = viewModel::decrementMaxAnte,
                )
            }
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: String?,
    iconTint: Color = PerkeoAccentRed,
    subtitle: String? = null,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold)
            if (value != null) Text(value, style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Bold)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
        Row {
            IconButton(onClick = onDecrement) { Icon(Icons.Default.Remove, null, tint = Color.White) }
            IconButton(onClick = onIncrement) { Icon(Icons.Default.Add, null, tint = Color.White) }
        }
    }
}

@Composable
private fun LegendarySection(state: FinderUiState, viewModel: FinderViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(PerkeoSurfaceDark)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (state.useLegendarySearch) Icons.Default.Speed else Icons.Default.BarChart,
            null, tint = PerkeoAccentRed, modifier = Modifier.size(32.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Use legendary search", style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Text("Every seed has a legendary joker", style = MaterialTheme.typography.labelSmall, color = Color.White)
            if (state.useLegendarySearch && state.cachedLegendaryCount > 0) {
                Text(
                    "Every seed has a legendary joker, but we are limited to ${state.cachedLegendaryCount} possible seeds",
                    style = MaterialTheme.typography.labelSmall, color = Color.White,
                )
            }
        }
        Switch(
            checked = state.useLegendarySearch,
            onCheckedChange = viewModel::toggleLegendary,
            colors = SwitchDefaults.colors(checkedThumbColor = PerkeoAccentRed, checkedTrackColor = PerkeoAccentRed.copy(alpha = 0.5f)),
        )
    }
}

@Composable
private fun InstantSection(state: FinderUiState, viewModel: FinderViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(PerkeoSurfaceDark)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (state.useInstantSearch) Icons.Default.Bolt else Icons.Default.Bolt,
            null, tint = PerkeoAccentRed, modifier = Modifier.size(32.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Use instant search", style = MaterialTheme.typography.bodyLarge, color = Color.White)
            Text("Selections will appear at any ante", style = MaterialTheme.typography.labelSmall, color = Color.White)
            if (state.useInstantSearch && state.cachedInstantCount > 0) {
                Text(
                    "Every seed has a Joker/Card of your selection, but we don't know the order. ${state.cachedInstantCount} possible seeds in the palm of your hand",
                    style = MaterialTheme.typography.labelSmall, color = Color.White,
                )
            }
        }
        Switch(
            checked = state.useInstantSearch,
            onCheckedChange = viewModel::toggleInstant,
            colors = SwitchDefaults.colors(checkedThumbColor = PerkeoAccentRed, checkedTrackColor = PerkeoAccentRed.copy(alpha = 0.5f)),
        )
    }
}

@Composable
private fun ClauseZoneView(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    clauses: List<DraggableItem>,
    onRemove: (String) -> Unit,
    onMove: (String) -> Unit,
    onMoveLabel: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PerkeoRowBackground)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("${clauses.size}", style = MaterialTheme.typography.bodyMedium, color = Color.White)
        }

        if (clauses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Drop items here", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
        } else {
            // Items in a wrap-like horizontal arrangement
            ClauseItemsRow(clauses = clauses, onRemove = onRemove, onMove = onMove, onMoveLabel = onMoveLabel)
        }
    }
}

@Composable
private fun ClauseItemsRow(
    clauses: List<DraggableItem>,
    onRemove: (String) -> Unit,
    onMove: (String) -> Unit,
    onMoveLabel: String,
) {
    val rows = clauses.chunked(4)
    for (row in rows) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (clause in row) {
                ClauseItemView(clause = clause, onRemove = onRemove, onMove = onMove, onMoveLabel = onMoveLabel)
            }
        }
    }
}

@Composable
private fun ClauseItemView(
    clause: DraggableItem,
    onRemove: (String) -> Unit,
    onMove: (String) -> Unit,
    onMoveLabel: String,
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.size(71.dp, 95.dp)) {
        val item: Item? = remember(clause) {
            try { clause.item() } catch (_: Exception) { null }
        }
        if (item != null) {
            SpriteView(
                item = item,
                showLabel = false,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { showMenu = true },
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(PerkeoSurfaceDark),
        ) {
            DropdownMenuItem(
                text = { Text(onMoveLabel, color = Color.White) },
                onClick = { onMove(clause.id); showMenu = false },
            )
            DropdownMenuItem(
                text = { Text("Remove", color = PerkeoAccentRed) },
                onClick = { onRemove(clause.id); showMenu = false },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoundSeedsSection(
    state: FinderUiState,
    viewModel: FinderViewModel,
    onNavigate: (String) -> Unit,
) {
    val sortedKeys = remember(state.foundSeeds, state.useLegendarySearch, state.useInstantSearch) {
        if (state.useLegendarySearch || state.useInstantSearch) {
            state.foundSeeds.keys.sortedByDescending { state.foundSeeds[it] ?: 0 }
        } else {
            state.foundSeeds.keys.toList().shuffled()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        // Disclosure group header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.toggleFoundSeeds() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Found Seeds (${state.foundSeeds.size})",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
            Icon(
                if (state.showFoundSeeds) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                null,
                tint = Color.White,
            )
        }

        if (state.showFoundSeeds) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(sortedKeys) { seed ->
                    val score = state.foundSeeds[seed] ?: 0
                    var dismissed by remember { mutableStateOf(false) }
                    AnimatedVisibility(visible = !dismissed) {
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { _ -> false }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {},
                            enableDismissFromStartToEnd = false,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PerkeoRowBackground)
                                    .clickable { onNavigate(seed) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(seed, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                                    if (state.useLegendarySearch || state.useInstantSearch) {
                                        Text("score: $score", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchProgressSheet(state: FinderUiState, onStop: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .background(PerkeoBackgroundDark)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TribouleteView()

            Text(
                text = if (state.seedsFoundCount == 0) "Searching..." else "${state.seedsFoundCount} seed found",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )

            if (!state.useLegendarySearch && !state.useInstantSearch) {
                LinearProgressIndicator(
                    progress = { if (state.seedsToAnalyze > 0) state.processedCount.toFloat() / state.seedsToAnalyze.toFloat() else 0f },
                    modifier = Modifier.fillMaxWidth(),
                    color = PerkeoAccentRed,
                )
                Text(
                    "${state.processedCount} / ${state.seedsToAnalyze}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(16.dp))

            HorizontalDivider(color = Color.DarkGray)

            TextButton(onClick = onStop) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Close, null, tint = PerkeoAccentRed)
                    Text("Stop", style = MaterialTheme.typography.bodyLarge, color = PerkeoAccentRed)
                }
            }
        }
    }
}
