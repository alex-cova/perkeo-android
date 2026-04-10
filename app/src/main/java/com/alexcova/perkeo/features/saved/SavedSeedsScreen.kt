package com.alexcova.perkeo.features.saved

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alexcova.perkeo.domain.model.SavedSeed
import com.alexcova.perkeo.domain.util.SeedFormat
import com.alexcova.perkeo.features.analyzer.AnalyzerViewModel
import com.alexcova.perkeo.ui.components.AnimatedTitle
import com.alexcova.perkeo.ui.sprite.SpriteView
import com.alexcova.perkeo.domain.engine.UnCommonJoker
import com.alexcova.perkeo.ui.theme.*
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedSeedsScreen(
    viewModel: SavedSeedsViewModel,
    analyzerViewModel: AnalyzerViewModel,
    onNavigateToSeed: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val pasteSeed = {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val text = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()?.trim()?.uppercase()
        when {
            text == null || !SeedFormat.isValid(text) ->
                toastMessage = "Not a valid seed"
            state.seeds.any { it.seed == text } ->
                toastMessage = "Seed already saved"
            else -> {
                analyzerViewModel.changeSeed(text)
                analyzerViewModel.storeSeed()
                onNavigateToSeed(text)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(PerkeoBackgroundDark)) {
        if (!state.isLoading && state.seeds.isEmpty()) {
            EmptySavedSeeds(onPaste = pasteSeed)
        } else {
            SeedsList(
                seeds = state.seeds,
                onPaste = pasteSeed,
                onSeedClick = onNavigateToSeed,
                onDelete = viewModel::deleteSeed,
            )
        }

        // Toast
        toastMessage?.let { msg ->
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(2000)
                toastMessage = null
            }
            Box(
                modifier = Modifier.fillMaxSize().padding(bottom = 32.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = PerkeoSurfaceDark,
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Text(
                        text = msg,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySavedSeeds(onPaste: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(PerkeoBackgroundDark),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedTitle(text = "Saved Seeds")
        Spacer(Modifier.weight(1f))
        SpriteView(
            item = UnCommonJoker.Joker_Stencil,
            showLabel = false,
            modifier = Modifier.size(71.dp, 95.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No saved seeds yet.",
            style = MaterialTheme.typography.bodyLarge,
            color = PerkeoTextMuted,
        )
        Spacer(Modifier.height(20.dp))
        OutlinedButton(
            onClick = onPaste,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, PerkeoAccentRed.copy(alpha = 0.6f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PerkeoAccentRed),
        ) {
            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Add from clipboard", style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeedsList(
    seeds: List<SavedSeed>,
    onPaste: () -> Unit,
    onSeedClick: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    val dateFormat = remember { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT) }

    Column(modifier = Modifier.fillMaxSize().background(PerkeoBackgroundDark)) {
        AnimatedTitle(text = "Saved Seeds")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                OutlinedButton(
                    onClick = onPaste,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, PerkeoAccentRed.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PerkeoAccentRed),
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add seed from clipboard", style = MaterialTheme.typography.bodyLarge)
                }
            }
            items(seeds, key = { it.seed }) { item ->
                var dismissed by remember { mutableStateOf(false) }
                AnimatedVisibility(visible = !dismissed) {
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                dismissed = true
                                onDelete(item.seed)
                                true
                            } else false
                        },
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        modifier = Modifier.clip(MaterialTheme.shapes.medium),
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(PerkeoAccentRed)
                                    .padding(end = 20.dp),
                                contentAlignment = Alignment.CenterEnd,
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        },
                        enableDismissFromStartToEnd = false,
                    ) {
                        SeedRow(
                            item = item,
                            dateFormat = dateFormat,
                            onClick = { onSeedClick(item.seed) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeedRow(
    item: SavedSeed,
    dateFormat: DateFormat,
    onClick: () -> Unit,
) {
    Surface(
        color = PerkeoSurfaceDark,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(item.seed, style = MaterialTheme.typography.titleLarge, color = Color.White)
            item.title?.let { title ->
                Text(title, style = MaterialTheme.typography.bodyMedium, color = PerkeoTextMuted)
            }
            Spacer(Modifier.height(2.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MetaChip(icon = Icons.Default.AccessTime, label = dateFormat.format(Date(item.timestamp)))
                item.level?.let { level ->
                    MetaChip(icon = Icons.Default.Flag, label = level)
                }
                item.score?.let { score ->
                    MetaChip(icon = Icons.Default.Star, label = "$score", iconTint = PerkeoAccentRed)
                }
            }
        }
    }
}

@Composable
private fun MetaChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconTint: Color = PerkeoTextMuted,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = iconTint)
        Text(label, style = MaterialTheme.typography.labelSmall, color = PerkeoTextMuted)
    }
}
