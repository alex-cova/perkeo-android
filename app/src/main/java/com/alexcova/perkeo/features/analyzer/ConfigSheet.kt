package com.alexcova.perkeo.features.analyzer

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .background(PerkeoBackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        // ── Quick actions ──
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickActionButton(
                icon = Icons.Default.ContentPaste,
                label = "Paste",
                onClick = { viewModel.paste(context) },
                modifier = Modifier.weight(1f),
            )
            QuickActionButton(
                icon = Icons.Default.ContentCopy,
                label = "Copy",
                onClick = { viewModel.copy(context) },
                modifier = Modifier.weight(1f),
            )
            QuickActionButton(
                icon = Icons.Default.Download,
                label = "Save",
                onClick = { viewModel.storeSeed() },
                modifier = Modifier.weight(1f),
            )
        }

        // ── Run setup ──
        SectionCard {
            StepperField(
                icon = Icons.Default.PlayArrow,
                iconTint = PerkeoAccentRed,
                label = "Starting ante: ${state.startingAnte}",
                onDecrement = {
                    viewModel.setStartingAnte((state.startingAnte - 1).coerceAtLeast(1))
                },
                onIncrement = {
                    val next = (state.startingAnte + 1).coerceAtMost(29)
                    viewModel.setStartingAnte(next)
                    if (next > state.maxAnte) viewModel.setMaxAnte(next)
                },
            )
            CardDivider()
            StepperField(
                icon = Icons.Default.Flag,
                iconTint = PerkeoTextMuted,
                label = "Max ante: ${state.maxAnte}",
                subtitle = "Deeper ante search is slower!",
                onDecrement = {
                    val next = (state.maxAnte - 1).coerceAtLeast(1)
                    viewModel.setMaxAnte(next)
                    if (next < state.startingAnte) viewModel.setStartingAnte(next.coerceAtLeast(1))
                },
                onIncrement = {
                    viewModel.setMaxAnte((state.maxAnte + 1).coerceAtMost(30))
                },
            )
            CardDivider()
            ToggleRow(
                title = "Showman",
                checked = state.showman,
                onCheckedChange = viewModel::setShowman,
            )
            CardDivider()
            SpriteDropdown(
                label = "Deck",
                selected = state.deck,
                options = Deck.allCases(),
                spriteSize = DpSize(24.dp, 32.dp),
                menuSpriteSize = DpSize(35.dp, 47.dp),
                onSelected = viewModel::setDeck,
            )
            CardDivider()
            SpriteDropdown(
                label = "Stake",
                selected = state.stake,
                options = Stake.entries,
                spriteSize = DpSize(24.dp, 24.dp),
                menuSpriteSize = DpSize(29.dp, 29.dp),
                onSelected = viewModel::setStake,
            )
        }

        // ── Vouchers ──
        SectionCard {
            ToggleRow(
                title = "Auto buy vouchers",
                checked = state.autoBuyVoucher,
                onCheckedChange = viewModel::setAutoBuyVoucher,
            )
        }
        if (!state.autoBuyVoucher) {
            SectionHeader(
                title = "Vouchers",
                subtitle = "Select the vouchers you have purchased",
            )
            DisclosureGroup(
                title = "Vouchers",
                items = Voucher.allCases(),
                isSelected = { viewModel.isSelected(it) },
                onToggle = { viewModel.toggleDisabledItem(it) },
                invertOpacity = true,
            )
        }

        // ── Owned jokers ──
        SectionHeader(
            title = "Owned jokers",
            subtitle = "Select the jokers you have already purchased",
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DisclosureGroup(
                title = "Legendary Jokers",
                items = LegendaryJoker.allCases(),
                isSelected = { viewModel.isSelected(it) },
                onToggle = { viewModel.toggleDisabledItem(it) },
            )
            DisclosureGroup(
                title = "Rare Jokers",
                items = RareJoker.allCases(),
                isSelected = { viewModel.isSelected(it) },
                onToggle = { viewModel.toggleDisabledItem(it) },
            )
            DisclosureGroup(
                title = "Uncommon Jokers",
                items = UnCommonJoker.allCases(),
                isSelected = { viewModel.isSelected(it) },
                onToggle = { viewModel.toggleDisabledItem(it) },
            )
            DisclosureGroup(
                title = "Common Jokers",
                items = CommonJoker.allCases(),
                isSelected = { viewModel.isSelected(it) },
                onToggle = { viewModel.toggleDisabledItem(it) },
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── Layout primitives ──────────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        color = PerkeoSurfaceDark,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(content = content)
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
            color = PerkeoTextMuted,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = PerkeoTextMuted.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun CardDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 1.dp,
        color = Color.White.copy(alpha = 0.06f),
    )
}

// ── Row components ─────────────────────────────────────────────────────────────

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = PerkeoSurfaceDark,
            contentColor = Color.White,
        ),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) {
            Icon(icon, contentDescription = null, tint = PerkeoAccentRed, modifier = Modifier.size(20.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun StepperField(
    icon: ImageVector,
    iconTint: Color,
    label: String,
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
        Icon(icon, contentDescription = null, tint = iconTint)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = Color.White)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = PerkeoTextMuted,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
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

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (subtitle != null) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = PerkeoTextMuted,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        } else {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
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

// ── Sprite dropdown ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun perkeoFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = PerkeoSurfaceDark,
    unfocusedContainerColor = PerkeoSurfaceDark,
    focusedBorderColor = PerkeoAccentRed,
    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
    focusedLabelColor = PerkeoAccentRed,
    unfocusedLabelColor = PerkeoTextMuted,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T : Item> SpriteDropdown(
    label: String,
    selected: T,
    options: List<T>,
    spriteSize: DpSize,
    menuSpriteSize: DpSize,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        OutlinedTextField(
            value = selected.rawValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = {
                Box(modifier = Modifier.padding(start = 8.dp)) {
                    SpriteView(
                        item = selected,
                        showLabel = false,
                        modifier = Modifier.size(spriteSize.width, spriteSize.height),
                    )
                }
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            colors = perkeoFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(PerkeoSurfaceDark),
        ) {
            options.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            SpriteView(
                                item = item,
                                showLabel = false,
                                modifier = Modifier.size(menuSpriteSize.width, menuSpriteSize.height),
                            )
                            Text(item.rawValue, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        }
                    },
                    onClick = { onSelected(item); expanded = false },
                )
            }
        }
    }
}

// ── Disclosure group ───────────────────────────────────────────────────────────

@Composable
private fun <T : Item> DisclosureGroup(
    title: String,
    items: List<T>,
    isSelected: (T) -> Boolean,
    onToggle: (T) -> Unit,
    invertOpacity: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
    val chevronAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "chevron",
    )
    SectionCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${items.count(isSelected)}/${items.size}",
                style = MaterialTheme.typography.labelMedium,
                color = PerkeoTextMuted,
                modifier = Modifier.padding(end = 8.dp),
            )
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = null,
                tint = PerkeoAccentRed,
                modifier = Modifier.rotate(chevronAngle),
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .padding(8.dp),
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
