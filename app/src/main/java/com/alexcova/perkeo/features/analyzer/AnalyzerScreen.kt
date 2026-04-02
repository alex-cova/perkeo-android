package com.alexcova.perkeo.features.analyzer

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alexcova.perkeo.domain.engine.*
import com.alexcova.perkeo.ui.sprite.SpriteView
import com.alexcova.perkeo.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzerScreen(
    viewModel: AnalyzerViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(modifier = modifier.fillMaxSize().background(PerkeoBackgroundDark)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AnalyzerTopBar(
                title = state.title, hasRun = state.run != null,
                onRandom = { viewModel.randomSeed() }, onPaste = { viewModel.paste(context) },
                onCopy = { viewModel.copy(context) }, onEnterSeed = { viewModel.enterSeed() },
                onDailyToday = { viewModel.seedOfTheDay() }, onSummary = { viewModel.toggleSummary() },
                onConfig = { viewModel.toggleConfig() },
            )
            Box(modifier = Modifier.weight(1f)) {
                if (state.run != null) PlayView(run = state.run!!, modifier = Modifier.fillMaxSize())
                else AnalyzerWelcome(onEnterSeed = { viewModel.enterSeed() })
                if (state.isLoading) LoadingOverlay()
            }
        }
        state.toast?.let { toast ->
            GameToast(toast = toast, onDismiss = { viewModel.dismissToast() },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp))
        }
    }

    if (state.showInput) {
        ModalBottomSheet(onDismissRequest = { viewModel.dismissInput() },
            containerColor = PerkeoBackgroundDark,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            SeedInputSheet(seed = state.seedInput, onSeedChange = viewModel::onSeedInputChanged,
                onAccept = { viewModel.analyze() })
        }
    }
    if (state.showConfig) {
        ModalBottomSheet(onDismissRequest = { viewModel.dismissConfig(); viewModel.analyze() },
            containerColor = PerkeoBackgroundDark,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)) {
            ConfigSheet(state = state, viewModel = viewModel, context = context)
        }
    }
    if (state.showSummary && state.run != null) {
        ModalBottomSheet(onDismissRequest = { viewModel.dismissSummary() },
            containerColor = PerkeoBackgroundDark,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            RunSummarySheet(run = state.run!!)
        }
    }
    if (state.showSaveView) {
        ModalBottomSheet(onDismissRequest = { viewModel.dismissSaveView() },
            containerColor = PerkeoBackgroundDark,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            SaveSeedSheet(state = state, onSave = { level, title -> viewModel.saveSeed(level, title) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzerTopBar(
    title: String, hasRun: Boolean,
    onRandom: () -> Unit, onPaste: () -> Unit, onCopy: () -> Unit,
    onEnterSeed: () -> Unit, onDailyToday: () -> Unit,
    onSummary: () -> Unit, onConfig: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = PerkeoBackgroundDark),
        actions = {
            Box {
                IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.Tune, null, tint = PerkeoAccentRed) }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(PerkeoSurfaceDark)) {
                    DropdownMenuItem(text = { Text("Random seed", fontFamily = GameFontFamily, color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.AutoAwesome, null, tint = Color.White) },
                        onClick = { showMenu = false; onRandom() })
                    DropdownMenuItem(text = { Text("Paste seed", fontFamily = GameFontFamily, color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.ContentPaste, null, tint = Color.White) },
                        onClick = { showMenu = false; onPaste() })
                    DropdownMenuItem(text = { Text("Copy seed", fontFamily = GameFontFamily, color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, null, tint = Color.White) },
                        onClick = { showMenu = false; onCopy() })
                    DropdownMenuItem(text = { Text("Enter a seed", fontFamily = GameFontFamily, color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = Color.White) },
                        onClick = { showMenu = false; onEnterSeed() })
                    DropdownMenuItem(text = { Text("Seed of the day", fontFamily = GameFontFamily, color = Color.White) },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, null, tint = Color.White) },
                        onClick = { showMenu = false; onDailyToday() })
                }
            }
            if (hasRun) IconButton(onClick = onSummary) { Icon(Icons.Default.Checklist, null, tint = PerkeoAccentRed) }
            IconButton(onClick = onConfig) { Icon(Icons.Default.Settings, null, tint = PerkeoAccentRed) }
        }
    )
}

@Composable
fun AnalyzerWelcome(onEnterSeed: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(PerkeoBackgroundDark).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        LegendaryJokerView(joker = LegendaryJoker.Perkeo)
        Spacer(Modifier.height(16.dp))
        Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("On the top right corner you will find the following options:", style = MaterialTheme.typography.labelMedium, color = Color.White)
            GameLabel("Seed Options (Copy, Paste, Random)", Icons.Default.Tune)
            GameLabel("Run Settings", Icons.Default.Settings)
            GameLabel("Seed summary", Icons.Default.Checklist)
        }
        Spacer(Modifier.height(16.dp))
        Button(onClick = onEnterSeed, colors = ButtonDefaults.buttonColors(containerColor = PerkeoAccentRed)) {
            Text("Enter a seed", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun LegendaryJokerView(joker: LegendaryJoker) {
    SpriteView(item = joker, modifier = Modifier.size(71.dp, 95.dp), showLabel = false)
}

@Composable
private fun GameLabel(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = PerkeoAccentRed, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = Color.White)
    }
}

@Composable
fun LoadingOverlay() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(60.dp), strokeWidth = 5.dp)
            Text("Processing...", style = MaterialTheme.typography.headlineSmall, color = Color.White)
        }
    }
}

@Composable
fun GameToast(toast: ToastData, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val color = when (toast.style) {
        ToastStyle.ERROR -> Color.Red; ToastStyle.WARNING -> Color(0xFFFFA000)
        ToastStyle.SUCCESS -> Color(0xFF4CAF50); ToastStyle.INFO -> Color(0xFF2196F3)
    }
    val icon = when (toast.style) {
        ToastStyle.ERROR -> Icons.Default.Cancel; ToastStyle.WARNING -> Icons.Default.Warning
        ToastStyle.SUCCESS -> Icons.Default.CheckCircle; ToastStyle.INFO -> Icons.Default.Info
    }
    LaunchedEffect(toast) { kotlinx.coroutines.delay(toast.duration); onDismiss() }
    Surface(modifier = modifier.padding(horizontal = 16.dp), shape = MaterialTheme.shapes.small,
        color = PerkeoBackgroundDark, border = BorderStroke(1.dp, color)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Text(toast.message, style = MaterialTheme.typography.labelMedium, color = Color.White, modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Close, null, tint = color, modifier = Modifier.size(16.dp))
            }
        }
    }
}

