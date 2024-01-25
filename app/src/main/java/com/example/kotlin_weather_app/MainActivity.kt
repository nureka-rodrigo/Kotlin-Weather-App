package com.example.kotlin_weather_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlin_weather_app.adapters.HourlyForecastAdapter
import com.example.kotlin_weather_app.adapters.WeatherItem

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)

        val itemList = listOf(
            WeatherItem("Now", R.drawable.cloudy_sunny, "25°"),
            WeatherItem("9 AM", R.drawable.cloudy_sunny, "25°"),
            WeatherItem("12 PM", R.drawable.cloudy_sunny, "25°"),
            WeatherItem("3 PM", R.drawable.cloudy_sunny, "25°"),
            WeatherItem("6 PM", R.drawable.cloudy_sunny, "25°"),
            WeatherItem("9 PM", R.drawable.cloudy_sunny, "25°"),
            WeatherItem("12 AM", R.drawable.cloudy_sunny, "25°"),
            WeatherItem("3 AM", R.drawable.cloudy_sunny, "25°"),
            WeatherItem("6 AM", R.drawable.cloudy_sunny, "25°"),
            )

        val adapter = HourlyForecastAdapter(itemList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }
}