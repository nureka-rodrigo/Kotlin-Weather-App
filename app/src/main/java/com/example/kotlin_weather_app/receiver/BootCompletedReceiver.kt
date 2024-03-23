package com.example.kotlin_weather_app.receiver

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.kotlin_weather_app.TimeAndWeather
import com.example.kotlin_weather_app.updateAppWidget

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, TimeAndWeather::class.java))

            val prefs = context.getSharedPreferences("widgetData", Context.MODE_PRIVATE)
            val data = prefs.getString("data", "{}")

            // There may be multiple widgets active, so update all of them
            for (appWidgetId in appWidgetIds) {
                if (data != null) {
                    updateAppWidget(context, appWidgetManager, appWidgetId , data)
                }
            }
        }
    }
}