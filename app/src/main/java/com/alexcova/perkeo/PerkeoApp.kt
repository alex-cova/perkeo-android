package com.alexcova.perkeo

import android.app.Application
import com.alexcova.perkeo.data.AppGraph
import com.alexcova.perkeo.integrations.shortcuts.ShortcutRegistrar
import com.alexcova.perkeo.ui.sprite.SpriteSheets

class PerkeoApp : Application() {
    lateinit var appGraph: AppGraph
        private set

    override fun onCreate() {
        super.onCreate()
        appGraph = AppGraph(this)
        SpriteSheets.init(this)
        ShortcutRegistrar.register(this)
    }
}
