package com.alexcova.perkeo.features.analyzer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.alexcova.perkeo.domain.engine.JokerType
import com.alexcova.perkeo.domain.engine.UnCommonJoker
import com.alexcova.perkeo.ui.sprite.SpriteView
import com.alexcova.perkeo.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveSeedSheet(
    state: AnalyzerUiState,
    onSave: (JokerType, String) -> Unit,
) {
    var description by remember { mutableStateOf("") }
    var level by remember { mutableStateOf(JokerType.COMMON) }
    var levelExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PerkeoBackgroundDark)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header: Certificate sprite + seed info
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SpriteView(item = UnCommonJoker.Certificate, modifier = Modifier.size(71.dp, 95.dp), showLabel = false)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Seed: ${state.seedInput}",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                )
                Text(
                    text = "Score: ${state.run?.score ?: 0}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                )
            }
        }

        Surface(color = PerkeoSurfaceDark, shape = MaterialTheme.shapes.small) {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Description", color = Color.White) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    capitalization = KeyboardCapitalization.Sentences,
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = PerkeoSurfaceDark,
                    unfocusedContainerColor = PerkeoSurfaceDark,
                ),
            )
        }

        // Level picker
        ExposedDropdownMenuBox(expanded = levelExpanded, onExpandedChange = { levelExpanded = it }) {
            OutlinedTextField(
                value = level.rawValue,
                onValueChange = {},
                readOnly = true,
                label = { Text("Level", color = Color.White) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = PerkeoSurfaceDark,
                    unfocusedContainerColor = PerkeoSurfaceDark,
                ),
            )
            ExposedDropdownMenu(
                expanded = levelExpanded,
                onDismissRequest = { levelExpanded = false },
                modifier = Modifier.background(PerkeoSurfaceDark),
            ) {
                JokerType.allCases().forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.rawValue, color = Color.White, style = MaterialTheme.typography.bodyLarge) },
                        onClick = { level = type; levelExpanded = false },
                    )
                }
            }
        }

        Button(
            onClick = { onSave(level, description) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PerkeoAccentRed),
        ) {
            Text("Save seed", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(Modifier.height(8.dp))
    }
}
