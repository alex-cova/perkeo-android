package com.alexcova.perkeo.features.finder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.alexcova.perkeo.domain.engine.*
import com.alexcova.perkeo.features.analyzer.AnalyzerViewModel
import com.alexcova.perkeo.ui.components.AnimatedTitle
import com.alexcova.perkeo.ui.components.TribouleteView
import com.alexcova.perkeo.ui.sprite.SpriteView
import com.alexcova.perkeo.ui.theme.*

private val MustColor = Color(0xFF4CAF50)
private val ShouldColor = Color(0xFFFFD600)

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
                    .background(PerkeoBackgroundDark)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Spacer(Modifier.height(4.dp))

                // ── Search mode cards ──
                if (!state.useLegendarySearch && !state.useInstantSearch) {
                    HeavySearchSection(state = state, viewModel = viewModel)
                }

                SearchModeCard(
                    icon = if (state.useLegendarySearch) Icons.Default.Speed else Icons.Default.BarChart,
                    title = "Legendary search",
                    subtitle = "Every seed has a legendary joker",
                    details = if (state.useLegendarySearch && state.cachedLegendaryCount > 0)
                        "Limited to ${state.cachedLegendaryCount} possible seeds" else null,
                    checked = state.useLegendarySearch,
                    onCheckedChange = viewModel::toggleLegendary,
                )

                SearchModeCard(
                    icon = Icons.Default.Bolt,
                    title = "Instant search",
                    subtitle = "Selections will appear at any ante",
                    details = if (state.useInstantSearch && state.cachedInstantCount > 0)
                        "${state.cachedInstantCount} possible seeds available" else null,
                    checked = state.useInstantSearch,
                    onCheckedChange = viewModel::toggleInstant,
                )

                if (state.loadingCache) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = PerkeoAccentRed,
                    )
                }

                // ── Joker selection area ──
                OutlinedButton(
                    onClick = { showJokerSelector = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, PerkeoAccentRed.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PerkeoAccentRed),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Select Jokers", style = MaterialTheme.typography.bodyLarge)
                }

                if (!state.isEmpty) {
                    ClauseZoneView(
                        title = "MUST",
                        subtitle = "Required",
                        icon = Icons.Default.CheckCircle,
                        color = MustColor,
                        clauses = state.must,
                        onRemove = viewModel::removeClause,
                        onMove = { id -> viewModel.addToClause(state.must.first { it.id == id }, ClauseType.SHOULD) },
                        onMoveLabel = "Move to SHOULD",
                    )
                    ClauseZoneView(
                        title = "SHOULD",
                        subtitle = "Bonus score",
                        icon = Icons.Default.Star,
                        color = ShouldColor,
                        clauses = state.should,
                        onRemove = viewModel::removeClause,
                        onMove = { id -> viewModel.addToClause(state.should.first { it.id == id }, ClauseType.MUST_NOT) },
                        onMoveLabel = "Move to MUST NOT",
                    )
                    ClauseZoneView(
                        title = "MUST NOT",
                        subtitle = "Banned",
                        icon = Icons.Default.Cancel,
                        color = PerkeoAccentRed,
                        clauses = state.mustNot,
                        onRemove = viewModel::removeClause,
                        onMove = { id -> viewModel.addToClause(state.mustNot.first { it.id == id }, ClauseType.MUST) },
                        onMoveLabel = "Move to MUST",
                    )
                }

                // ── Action buttons ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!state.foundSeeds.isEmpty() || !state.isEmpty) {
                        TextButton(
                            onClick = viewModel::clearAll,
                            colors = ButtonDefaults.textButtonColors(contentColor = PerkeoTextMuted),
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Clear", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    if (!state.isEmpty) {
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = viewModel::startSearch,
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MustColor,
                                contentColor = Color.White,
                            ),
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Search", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                // ── Found seeds ──
                if (state.foundSeeds.isNotEmpty()) {
                    FoundSeedsSection(state = state, viewModel = viewModel, onNavigate = onNavigateToSeed)
                } else {
                    Spacer(Modifier.height(60.dp))
                }

                Spacer(Modifier.height(16.dp))
            }
        }

        // ── Search progress overlay ──
        if (state.searching) {
            SearchProgressSheet(state = state, onStop = viewModel::stopSearch)
        }
    }

    // ── Joker selector bottom sheet ──
    if (showJokerSelector) {
        ModalBottomSheet(
            onDismissRequest = { showJokerSelector = false },
            containerColor = PerkeoBackgroundDark,
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

// ── Search mode components ────────────────────────────────────────────────────

@Composable
private fun SearchModeCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    details: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        color = PerkeoSurfaceDark,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = PerkeoAccentRed, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = PerkeoTextMuted)
                if (details != null) {
                    Text(details, style = MaterialTheme.typography.labelSmall, color = PerkeoTextMuted)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PerkeoAccentRed,
                    checkedTrackColor = PerkeoAccentRed.copy(alpha = 0.5f),
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeavySearchSection(state: FinderUiState, viewModel: FinderViewModel) {
    Surface(
        color = PerkeoSurfaceDark,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleHeavyControls() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Heavy Search",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PerkeoAccentRed,
                    modifier = Modifier.weight(1f),
                )
                val rotation by animateFloatAsState(
                    targetValue = if (state.showHeavyControls) 180f else 0f,
                    label = "chevron",
                )
                Icon(Icons.Default.ExpandMore, contentDescription = null, tint = PerkeoAccentRed, modifier = Modifier.rotate(rotation))
            }

            AnimatedVisibility(
                visible = state.showHeavyControls,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = Color.White.copy(alpha = 0.06f))
                    StepperRow(
                        label = "Seeds to analyze",
                        value = "${state.seedsToAnalyze}",
                        onDecrement = viewModel::decrementSeedsToAnalyze,
                        onIncrement = viewModel::incrementSeedsToAnalyze,
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = Color.White.copy(alpha = 0.06f))
                    StepperRow(
                        label = "Starting ante: ${state.startingAnte}",
                        value = null,
                        onDecrement = viewModel::decrementStartingAnte,
                        onIncrement = viewModel::incrementStartingAnte,
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = Color.White.copy(alpha = 0.06f))
                    StepperRow(
                        label = "Last ante: ${state.maxAnte}",
                        value = null,
                        subtitle = if (state.isHeavySearch) "This might take a while" else null,
                        onDecrement = viewModel::decrementMaxAnte,
                        onIncrement = viewModel::incrementMaxAnte,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StepperRow(
    label: String,
    value: String?,
    subtitle: String? = null,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = Color.White)
            if (value != null) Text(value, style = MaterialTheme.typography.titleMedium, color = PerkeoAccentRed)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.labelSmall, color = PerkeoTextMuted)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FilledIconButton(
                onClick = onDecrement,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = PerkeoRowBackground,
                    contentColor = Color.White,
                ),
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            FilledIconButton(
                onClick = onIncrement,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = PerkeoRowBackground,
                    contentColor = Color.White,
                ),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ── Clause zones ──────────────────────────────────────────────────────────────

@Composable
private fun ClauseZoneView(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    clauses: List<DraggableItem>,
    onRemove: (String) -> Unit,
    onMove: (String) -> Unit,
    onMoveLabel: String,
) {
    Surface(
        color = PerkeoSurfaceDark,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.bodyLarge, color = Color.White, modifier = Modifier.weight(1f))
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = PerkeoTextMuted)
                Spacer(Modifier.width(8.dp))
                Text("${clauses.size}", style = MaterialTheme.typography.labelMedium, color = color)
            }

            if (clauses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.08f), MaterialTheme.shapes.small)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Empty", style = MaterialTheme.typography.labelMedium, color = PerkeoTextMuted.copy(alpha = 0.5f))
                }
            } else {
                ClauseItemsRow(clauses = clauses, onRemove = onRemove, onMove = onMove, onMoveLabel = onMoveLabel)
            }
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
                modifier = Modifier.fillMaxSize().clickable { showMenu = true },
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

// ── Found seeds ───────────────────────────────────────────────────────────────

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
    val chevronAngle by animateFloatAsState(
        targetValue = if (state.showFoundSeeds) 180f else 0f,
        label = "found_chevron",
    )

    Surface(
        color = PerkeoSurfaceDark,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleFoundSeeds() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Found Seeds",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${state.foundSeeds.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = PerkeoTextMuted,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = PerkeoAccentRed,
                    modifier = Modifier.rotate(chevronAngle),
                )
            }

            AnimatedVisibility(
                visible = state.showFoundSeeds,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 600.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(sortedKeys) { seed ->
                        val score = state.foundSeeds[seed] ?: 0
                        Surface(
                            color = PerkeoBackgroundDark,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigate(seed) },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(seed, style = MaterialTheme.typography.titleMedium, color = Color.White)
                                    if (state.useLegendarySearch || state.useInstantSearch) {
                                        Text(
                                            "Score: $score",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PerkeoTextMuted,
                                        )
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PerkeoTextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Search progress overlay ───────────────────────────────────────────────────

@Composable
private fun SearchProgressSheet(state: FinderUiState, onStop: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            color = PerkeoBackgroundDark,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(0.85f),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
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
                        progress = {
                            if (state.seedsToAnalyze > 0)
                                state.processedCount.toFloat() / state.seedsToAnalyze.toFloat()
                            else 0f
                        },
                        modifier = Modifier.fillMaxWidth(),
                        color = PerkeoAccentRed,
                    )
                    Text(
                        "${state.processedCount} / ${state.seedsToAnalyze}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PerkeoTextMuted,
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                OutlinedButton(
                    onClick = onStop,
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, PerkeoAccentRed.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PerkeoAccentRed),
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Stop", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
