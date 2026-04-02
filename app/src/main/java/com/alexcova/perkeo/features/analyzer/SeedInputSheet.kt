package com.alexcova.perkeo.features.analyzer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alexcova.perkeo.domain.engine.LegendaryJoker
import com.alexcova.perkeo.ui.sprite.SpriteView
import com.alexcova.perkeo.ui.theme.*

@Composable
fun SeedInputSheet(
    seed: String,
    onSeedChange: (String) -> Unit,
    onAccept: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(PerkeoBackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        SpriteView(item = LegendaryJoker.Canio, modifier = Modifier.size(71.dp, 95.dp), showLabel = false)

        Text(
            text = "Enter Seed",
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
        )

        OutlinedTextField(
            value = seed,
            onValueChange = onSeedChange,
            modifier = Modifier.width(200.dp),
            textStyle = MaterialTheme.typography.displaySmall.copy(textAlign = TextAlign.Center),
            placeholder = { Text("Seed", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onAccept() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.Gray,
                unfocusedContainerColor = Color.Gray,
            ),
        )

        Button(
            onClick = onAccept,
            colors = ButtonDefaults.buttonColors(containerColor = PerkeoAccentRed),
        ) {
            Text("Accept", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(Modifier.height(16.dp))
    }
}
