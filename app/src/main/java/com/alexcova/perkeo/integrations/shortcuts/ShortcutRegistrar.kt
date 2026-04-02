package com.alexcova.perkeo.integrations.shortcuts

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import com.alexcova.perkeo.R

object ShortcutRegistrar {
    private const val randomSeedShortcutId = "copy_random_seed_dynamic"

    fun register(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return
        val manager = context.getSystemService(ShortcutManager::class.java) ?: return

        val shortcut = ShortcutInfo.Builder(context, randomSeedShortcutId)
            .setShortLabel(context.getString(R.string.shortcut_copy_seed_short))
            .setLongLabel(context.getString(R.string.shortcut_copy_seed_long))
            .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(
                Intent(context, CopyRandomSeedReceiver::class.java).setAction(
                    "com.alexcova.perkeo.action.COPY_RANDOM_SEED",
                ),
            )
            .build()

        manager.dynamicShortcuts = listOf(shortcut)
    }
}

