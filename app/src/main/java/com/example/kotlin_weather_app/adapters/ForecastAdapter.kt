package com.example.kotlin_weather_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlin_weather_app.R

// Data class to hold the weather information for each hour
data class WeatherItem(
    val title: String,
    val iconResId: Int,
    val temperature: String,
    val main: String
)

// Adapter class for the RecyclerView that displays the hourly forecast
class HourlyForecastAdapter(private val itemList: MutableList<WeatherItem>) :
    RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder>() {

    // ViewHolder class that holds the views for each item in the RecyclerView
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textView18)
        val iconImageView: ImageView = itemView.findViewById(R.id.imageView5)
        val temperatureTextView: TextView = itemView.findViewById(R.id.textView19)
    }

    // This function is called when RecyclerView needs a new ViewHolder of the given type to represent an item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.viewholder_forecast, parent, false)
        return ViewHolder(view)
    }

    // This function is called by RecyclerView to display the data at the specified position
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.titleTextView.text = currentItem.title
        holder.iconImageView.setImageResource(currentItem.iconResId)
        holder.temperatureTextView.text = currentItem.temperature
    }

    // This function returns the total number of items in the data set held by the adapter
    override fun getItemCount(): Int {
        return itemList.size
    }

    // This function is used to update the data in the adapter
    fun setItems(weatherItems: MutableList<WeatherItem>) {
        val oldSize = itemList.size
        itemList.clear()
        itemList.addAll(weatherItems)
        for (i in oldSize until itemList.size) {
            notifyItemInserted(i)
        }
    }
}