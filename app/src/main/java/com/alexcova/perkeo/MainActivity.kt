package com.alexcova.perkeo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.alexcova.perkeo.ui.theme.PerkeoTheme
import com.alexcova.perkeo.ui.root.RootScaffold

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PerkeoTheme {
                RootScaffold(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
