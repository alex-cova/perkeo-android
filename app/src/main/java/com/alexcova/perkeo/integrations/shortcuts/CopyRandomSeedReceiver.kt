package com.alexcova.perkeo.integrations.shortcuts

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.alexcova.perkeo.R
import com.alexcova.perkeo.domain.util.RandomSeedGenerator

class CopyRandomSeedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val randomSeed = RandomSeedGenerator.generate()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("seed", randomSeed))
        Toast.makeText(
            context,
            context.getString(R.string.shortcut_seed_copied, randomSeed),
            Toast.LENGTH_SHORT,
        ).show()
    }
}

