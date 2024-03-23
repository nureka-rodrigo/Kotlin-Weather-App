package com.example.kotlin_weather_app

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class FetchDataWorker(private val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {
    private val _openWeatherMapApiAky = "668c2a5ed2549b7f50600493623ca749"
    private val prefs = applicationContext.getSharedPreferences("widgetData", Context.MODE_PRIVATE)
    override suspend fun doWork(): Result {
        val latitude = prefs.getString("latitude", "51.5072")
        val longitude = prefs.getString("longitude", "-0.1276")

        if (latitude != null && longitude != null) {
            if (latitude.toDouble() != 0.0 && longitude.toDouble() != 0.0) {
                val url =
                    "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&units=metric&appid=$_openWeatherMapApiAky"

                val request = JsonObjectRequest(
                    Request.Method.GET, url, null,
                    { response ->
                        prefs.edit().putString("data", response.toString()).apply()
                    },
                    { _ ->
                    })

                // Add the request to the RequestQueue.
                Volley.newRequestQueue(context).add(request)
            }
        }
        return Result.success()
    }
}

/**
 * Implementation of App Widget functionality.
 */
open class TimeAndWeather : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs = context.getSharedPreferences("widgetData", Context.MODE_PRIVATE)
        val data = prefs.getString("data", "{}")

        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            if (data != null) {
                updateAppWidget(context, appWidgetManager, appWidgetId, data)
            }
        }

        // Schedule the next update
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, javaClass).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Calculate the time until the next minute
        val calendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExact(AlarmManager.RTC, calendar.timeInMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC, calendar.timeInMillis, pendingIntent)
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC, calendar.timeInMillis, pendingIntent)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
        val workRequest = PeriodicWorkRequestBuilder<FetchDataWorker>(15, TimeUnit.MINUTES)
            .setInitialDelay(0, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "FetchDataWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    // Function to get weather icon
    open fun getWeatherIcon(main: String): Int {
        return when (main) {
            "Thunderstorm" -> R.drawable.thunderstorm
            "Drizzle" -> R.drawable.drizzle
            "Rain" -> R.drawable.rain
            "Snow" -> R.drawable.snow
            "Mist", "Smoke", "Haze", "Dust", "Fog", "Sand", "Ash", "Squall", "Tornado" -> R.drawable.mist
            "Clear" -> R.drawable.clear
            "Clouds" -> R.drawable.clouds
            else -> R.drawable.clear // Default icon
        }
    }

    // Function to update the widget
   open fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        data: String
    ) {

    }
}
