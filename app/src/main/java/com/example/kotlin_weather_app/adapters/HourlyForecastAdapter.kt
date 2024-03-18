package com.example.kotlin_weather_app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlin_weather_app.R

data class WeatherItem(val title: String, val iconResId: Int, val temperature: String)

class HourlyForecastAdapter(private val itemList: MutableList<WeatherItem>) :
    RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.textView12)
        val iconImageView: ImageView = itemView.findViewById(R.id.imageView5)
        val temperatureTextView: TextView = itemView.findViewById(R.id.textView13)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.viewholder_hourly, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.titleTextView.text = currentItem.title
        holder.iconImageView.setImageResource(currentItem.iconResId)
        holder.temperatureTextView.text = currentItem.temperature
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun setItems(weatherItems: MutableList<WeatherItem>) {
        val oldSize = itemList.size
        itemList.clear()
        itemList.addAll(weatherItems)
        for (i in oldSize until itemList.size) {
            notifyItemInserted(i)
        }
    }
}

