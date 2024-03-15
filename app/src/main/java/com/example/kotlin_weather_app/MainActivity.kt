package com.example.kotlin_weather_app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlin_weather_app.adapters.HourlyForecastAdapter
import com.example.kotlin_weather_app.adapters.WeatherItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val _permissionRequestAccessFineLocation = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private val OPEN_WEATHER_MAP_API_KEY = "668c2a5ed2549b7f50600493623ca749"
    private lateinit var adapter: HourlyForecastAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        adapter = HourlyForecastAdapter(mutableListOf()) // Initialize adapter with an empty list
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // check permission is granted or not
        getLocationPermission()

        // get current location
        getLocation();



        val itemList = mutableListOf(
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

//        val adapter = HourlyForecastAdapter(itemList)
//        recyclerView.adapter = adapter
//        recyclerView.layoutManager =
//            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        //fetch weather data
        fetchWeatherData()
    }



    // check permission is granted or not
    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                _permissionRequestAccessFineLocation)
        }
    }

    // handle permission request result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            _permissionRequestAccessFineLocation -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission was granted
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    // Permission was denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    // check gps is active or not
    private fun isGpsEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun getLocation() {
        // check gps is active or not : if not active then ask user to enable it
        if (isGpsEnabled()) {
            // get current location
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            // check permission is granted or not again
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        // Use the location
                        latitude = location.latitude
                        longitude = location.longitude
                        /*
                         *  Remove this toast message after testing
                         */
                        Toast.makeText(this, "Latitude: $latitude, Longitude: $longitude", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        fusedLocationClient.getCurrentLocation(100, null)
                            .addOnSuccessListener { currentLocation ->
                                if (currentLocation != null) {
                                    // Use the location
                                    latitude = currentLocation.latitude
                                    longitude = currentLocation.longitude
                                    /*
                                    *  Remove this toast message after testing
                                    */
                                    Toast.makeText(this, "Latitude: $latitude, Longitude: $longitude", Toast.LENGTH_SHORT).show()
                                }
                                else {
                                    Toast.makeText(this, "Something went wrong...", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                }
        } else {
            // ask user to enable gps
            AlertDialog.Builder(this)
                .setMessage("GPS is disabled. Do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->
                    startActivity(android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun fetchWeatherData() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude
                Log.i("lat",lat.toString())
                Log.i("lgt",lon.toString())
                val url = "https://api.openweathermap.org/data/2.5/forecast?lat=$lat&lon=$lon&appid=$OPEN_WEATHER_MAP_API_KEY"

                val request = JsonObjectRequest(Request.Method.GET, url, null,
                    Response.Listener { response ->
                        handleWeatherResponse(response)
                    },
                    Response.ErrorListener { error ->
                        showError("Failed to fetch weather data: ${error.message}")
                    })

                // Add the request to the RequestQueue.
                Volley.newRequestQueue(this).add(request)
            } else {
                showError("Failed to get location")
            }
        }
    }


    private fun handleWeatherResponse(response: JSONObject) {
        val forecastItems = response.optJSONArray("list")
        val weatherItems = mutableListOf<WeatherItem>()

        if (forecastItems != null) {
            val currentDate = getCurrentDate()
            for (i in 0 until forecastItems.length()) {
                val forecastItem = forecastItems.optJSONObject(i)

                if (forecastItem != null) {
                    val main = forecastItem.optJSONObject("main")
                    val weatherArray = forecastItem.optJSONArray("weather")
                    val weather = weatherArray?.optJSONObject(0)

                    val time = forecastItem.optString("dt")
                    val fulltime = forecastItem.optString("dt_txt")

                    val forecastDate = getFormattedDate(fulltime)
                    if (forecastDate == currentDate) {
                        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                        val date = Date(time.toLong())
                        val newtime=sdf.format(date)
                        val icon = weather?.optString("icon") ?: ""
                        val temp = main?.optDouble("temp")?.minus(273.15) ?: 0.0

                        val weatherItem = WeatherItem(newtime, R.drawable.cloudy_sunny, "${temp.toInt()}°C")
                        weatherItems.add(weatherItem)
                    }



                }
            }
        }

        val weatherDescription: TextView = findViewById(R.id.weatherDescription)
        val forcastdata = forecastItems.optJSONObject(0)
        val weatherArray = forcastdata.optJSONArray("weather")
        val weatherdata = weatherArray?.optJSONObject(0)
        val description = weatherdata?.optString("description") ?: ""
        weatherDescription.text=description

        val cityNameText: TextView = findViewById(R.id.cityName)
        val city = response.optJSONObject("city")
        val cityname = city?.optString("name") ?: ""
        cityNameText.text=cityname

        val maindata = forcastdata.optJSONObject("main")
        val temp = maindata?.optDouble("temp")?.minus(273.15) ?: 0.0
        val tempText: TextView = findViewById(R.id.temp)
        tempText.text="${temp.toInt()}°C"

        val tempmax_min_Text: TextView = findViewById(R.id.temp_max_min)
        val tempmax = maindata?.optDouble("temp_max")?.minus(273.15) ?: 0.0
        val tempmin = maindata?.optDouble("temp_min")?.minus(273.15) ?: 0.0
        tempmax_min_Text.text="${tempmax.toInt()}° / ${tempmin.toInt()}° "

        val preasure = maindata?.optString("pressure") ?: ""
        val pressureText: TextView = findViewById(R.id.preasure)
        pressureText.text="${preasure}mmHg"

        val humid = maindata?.optString("humidity") ?: ""
        val humidText: TextView = findViewById(R.id.humid)
        humidText.text="${humid}%"

        adapter.setItems(weatherItems)


    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = Date()
        return sdf.format(currentDate)
    }

    private fun getFormattedDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        return outputFormat.format(date)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }






}