package com.alexcova.perkeo.integrations.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.alexcova.perkeo.MainActivity
import com.alexcova.perkeo.R
import com.alexcova.perkeo.domain.util.DailySeedGenerator
import java.time.LocalDate

class PerkeoWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            appWidgetManager.updateAppWidget(appWidgetId, buildViews(context))
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        updateAll(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            updateAll(context)
        }
    }

    private fun updateAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, PerkeoWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        ids.forEach { manager.updateAppWidget(it, buildViews(context)) }
    }

    private fun buildViews(context: Context): RemoteViews {
        val seed = DailySeedGenerator.generate(LocalDate.now())
        return RemoteViews(context.packageName, R.layout.widget_perkeo).apply {
            setTextViewText(R.id.widget_title, context.getString(R.string.widget_title))
            setTextViewText(R.id.widget_seed, seed)
            setOnClickPendingIntent(
                R.id.widget_root,
                PendingIntent.getActivity(
                    context,
                    1001,
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
        }
    }
}

