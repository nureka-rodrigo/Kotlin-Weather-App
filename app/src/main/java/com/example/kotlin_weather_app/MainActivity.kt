package com.example.kotlin_weather_app

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.kotlin_weather_app.adapters.HourlyForecastAdapter
import com.example.kotlin_weather_app.adapters.WeatherItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "locations")

class MainActivity : AppCompatActivity() {
    // Constants
    private val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1
    private val OPEN_WEATHER_MAP_API_KEY = "668c2a5ed2549b7f50600493623ca749"
    private lateinit var weatherResponse: JSONObject
    private lateinit var airQualityResponse: JSONObject
    private val dataStoreLat = doublePreferencesKey("latitude")
    private val dataStoreLng = doublePreferencesKey("longitude")
    private lateinit var turnOnLocationBtn: MaterialCardView

    // Variables
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var adapter: HourlyForecastAdapter

    // This function is called when the activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            val lat: Flow<Double> =
                dataStore.data.map { preferences -> preferences[dataStoreLat] ?: 51.5072 }
            val lng: Flow<Double> =
                dataStore.data.map { preferences -> preferences[dataStoreLng] ?: -0.1276 }
            runBlocking {
                fetchAirQualityData(lat.first(), lng.first())
                fetchWeatherData(lat.first(), lng.first())
            }
            setKeepOnScreenCondition {
                !::weatherResponse.isInitialized || !::airQualityResponse.isInitialized
            }

            setOnExitAnimationListener { splashScreenView ->
                handleWeatherResponse(weatherResponse)
                handleAirQualityResponse(airQualityResponse)
                splashScreenView.remove()
            }
        }

        setContentView(R.layout.activity_main)
        turnOnLocationBtn = findViewById(R.id.location_on_btn)

        // Set up SwipeRefreshLayout
        val swipeRefreshLayout: SwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            // Get current location
            getCurrentLocation()
            swipeRefreshLayout.isRefreshing = false
        }

        // Set up RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        adapter = HourlyForecastAdapter(mutableListOf()) // Initialize adapter with an empty list
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        // Get current location
        getCurrentLocation()

        // Set up turn on location button
        turnOnLocationBtn.setOnClickListener {
            startActivity(android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    // Function to check if location permission is granted
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    // Function to handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_ACCESS_FINE_LOCATION -> {
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

    // Function to check if GPS is enabled
    private fun isGpsEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    // Function to get current location
    private fun getCurrentLocation() {
        // Check if GPS is enabled
        if (isGpsEnabled()) {
            // Get the location service
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            // Check if location permission is granted
            checkLocationPermission()

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        turnOnLocationBtn.visibility = View.GONE
                        val latitude = location.latitude
                        val longitude = location.longitude
                        fetchWeatherData(latitude, longitude)
                        fetchAirQualityData(latitude, longitude)
                    } else {
                        fusedLocationClient.getCurrentLocation(100, null)
                            .addOnSuccessListener { currentLocation ->
                                if (currentLocation != null) {
                                    turnOnLocationBtn.visibility = View.GONE
                                    val latitude = currentLocation.latitude
                                    val longitude = currentLocation.longitude
                                    fetchWeatherData(latitude, longitude)
                                    fetchAirQualityData(latitude, longitude)
                                }
                            }
                    }
                }
        } else {
            turnOnLocationBtn.visibility = View.VISIBLE
        }
    }

    // Function to fetch weather data
    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        if (latitude != 0.0 && longitude != 0.0) {
            val url =
                "https://api.openweathermap.org/data/2.5/forecast?lat=$latitude&lon=$longitude&units=metric&appid=$OPEN_WEATHER_MAP_API_KEY"

            val request = JsonObjectRequest(Request.Method.GET, url, null,
                { response ->
                    weatherResponse = response
                    handleWeatherResponse(response)
                },
                { error ->
                    showError("Failed to fetch weather data: ${error.message}")
                })

            // Add the request to the RequestQueue.
            Volley.newRequestQueue(this).add(request)
        } else
            showError("Failed to get weather data")
    }

    // Function to fetch air quality data
    private fun fetchAirQualityData(latitude: Double, longitude: Double) {
        if (latitude != 0.0 && longitude != 0.0) {
            // Save location to dataStore
            runBlocking {
                saveLocation(latitude, longitude)
            }
            val url =
                "https://api.openweathermap.org/data/2.5/air_pollution?lat=$latitude&lon=$longitude&appid=$OPEN_WEATHER_MAP_API_KEY"

            val request = JsonObjectRequest(Request.Method.GET, url, null,
                { response ->
                    airQualityResponse = response
                    handleAirQualityResponse(response)
                },
                { error ->
                    showError("Failed to fetch air quality data: ${error.message}")
                })

            // Add the request to the RequestQueue.
            Volley.newRequestQueue(this).add(request)
        } else
            showError("Failed to get weather data")
    }

    // Function to show last update time
    private fun lastUpdate() {
        val lastUpdatedTextView: TextView = findViewById(R.id.lastUpdatedTextView)
        val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        lastUpdatedTextView.text = getString(R.string.last_updated, currentTime)
    }

    // Function to handle weather response
    private fun handleWeatherResponse(response: JSONObject) {
        val forecastItems = response.optJSONArray("list")
        val weatherItems = mutableListOf<WeatherItem>()

        val currentTime = System.currentTimeMillis() / 1000 // Current Unix timestamp
        val nextDayTime = currentTime + 86400 // Unix timestamp for the same time tomorrow

        if (forecastItems != null) {
            for (i in 0 until forecastItems.length()) {
                val forecastItem = forecastItems.optJSONObject(i)
                if (forecastItem != null) {
                    val time = forecastItem.optLong("dt")

                    // Only consider forecast items for the next 24 hours
                    if (time < nextDayTime) {
                        val main = forecastItem.optJSONObject("main")
                        val weatherArray = forecastItem.optJSONArray("weather")
                        val weather = weatherArray?.optJSONObject(0)
                        val mainString = weather?.optString("main") ?: ""

                        val date = Date(time * 1000L) // Convert to milliseconds
                        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                        var formattedTime = sdf.format(date)

                        // If this is the first item, set the time to "Now"
                        if (i == 0) {
                            formattedTime = "Now"
                        }

                        val temp = main?.optDouble("temp") ?: 0.0

                        val weatherItem =
                            WeatherItem(
                                formattedTime,
                                getWeatherIcon(mainString),
                                "${temp.toInt()}°C",
                                mainString
                            )
                        weatherItems.add(weatherItem)
                    }
                }
            }
        }

        // Get current date
        val currentDate = SimpleDateFormat("EEE MMM dd", Locale.getDefault()).format(Date())
        val dateTextView: TextView = findViewById(R.id.textView1)
        dateTextView.text = currentDate

        // Get weather description
        val weatherDescription: TextView = findViewById(R.id.weatherDescription)

        val forecastData = forecastItems?.optJSONObject(0)
        val weatherArray = forecastData?.optJSONArray("weather")
        val weatherData = weatherArray?.optJSONObject(0)
        val main = weatherData?.optString("main") ?: ""

        // Get weather icon
        val weatherIcon = getWeatherIcon(main)
        val weatherImageView: ImageView = findViewById(R.id.imageView)
        weatherImageView.setImageResource(weatherIcon)
        val description = weatherData?.optString("description")?.lowercase(Locale.getDefault())
            ?.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
            ?: ""
        weatherDescription.text = getString(R.string.weather_description, description)

        // Get city name
        val cityNameText: TextView = findViewById(R.id.cityName)
        val city = response.optJSONObject("city")
        val cityName = city?.optString("name") ?: ""
        cityNameText.text = getString(R.string.city, cityName)

        val mainData = forecastData?.optJSONObject("main")

        // Get temperature data
        val temp = mainData?.optDouble("temp") ?: 0.0
        val tempText: TextView = findViewById(R.id.temp)
        tempText.text = getString(R.string.temp, temp.toInt())

        // Get feels like temperature data
        val feelsLike = mainData?.optDouble("feels_like") ?: 0.0
        val feelsLikeTextView: TextView = findViewById(R.id.feels_like)
        feelsLikeTextView.text = getString(R.string.feels_like, feelsLike.toInt())

        // Get max and min temperature data
        val tempMax = mainData?.optDouble("temp_max") ?: 0.0
        val tempMin = mainData?.optDouble("temp_min") ?: 0.0
        val tempMaxMinText: TextView = findViewById(R.id.temp_max_min)
        tempMaxMinText.text = getString(R.string.temp_min_max, tempMax.toInt(), tempMin.toInt())

        // Get pressure data
        val pressure = mainData?.optString("pressure") ?: ""
        val pressureText: TextView = findViewById(R.id.preasure)
        pressureText.text = getString(R.string.pressure_value, pressure)

        // Get cloudiness data
        val cloudsData = forecastData?.optJSONObject("clouds")
        val cloudiness = cloudsData?.optInt("all") ?: 0
        val cloudinessText: TextView = findViewById(R.id.cloudiness)
        cloudinessText.text = getString(R.string.cloudiness_value, cloudiness)

        // Get humidity data
        val humidity = mainData?.optString("humidity") ?: ""
        val humidText: TextView = findViewById(R.id.humid)
        humidText.text = getString(R.string.humidity_value, humidity)

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        // Get sunrise time
        val sunriseTimestamp = response.getJSONObject("city").getLong("sunrise")
        val sunriseDate = Date(sunriseTimestamp * 1000L) // Convert to milliseconds
        val sunriseTime = timeFormat.format(sunriseDate)
        val sunriseTextView: TextView = findViewById(R.id.textView12)
        sunriseTextView.text = getString(R.string.sunrise, sunriseTime)

        // Get sunset time
        val sunsetTimestamp = response.getJSONObject("city").getLong("sunset")
        val sunsetDate = Date(sunsetTimestamp * 1000L) // Convert to milliseconds
        val sunsetTime = timeFormat.format(sunsetDate)
        val sunsetTextView: TextView = findViewById(R.id.textView13)
        sunsetTextView.text = getString(R.string.sunset, sunsetTime)

        // Change background image based on weather
        changeBackgroundImage(main, sunriseDate, sunsetDate)

        // Get wind data
        val windData = forecastData?.optJSONObject("wind")
        val windSpeed = windData?.optDouble("speed") ?: 0.0
        val windDirectionDegrees = windData?.optDouble("deg") ?: 0.0

        // Get wind speed
        val windSpeedText: TextView = findViewById(R.id.textView15)
        windSpeedText.text = getString(R.string.wind_speed, windSpeed)

        // Get wind direction
        val windDirectionText: TextView = findViewById(R.id.textView14)
        windDirectionText.text = getString(
            R.string.wind_direction,
            convertDegreesToCardinalDirection(windDirectionDegrees)
        )

        adapter.setItems(weatherItems)
    }

    // Function to handle air quality response
    private fun handleAirQualityResponse(response: JSONObject) {
        // Get air quality items
        val airQualityItems = response.optJSONArray("list")

        if (airQualityItems != null && airQualityItems.length() > 0) {
            // Get main and components objects
            val airQualityItem = airQualityItems.optJSONObject(0)
            val main = airQualityItem.optJSONObject("main")
            val components = airQualityItem.optJSONObject("components")

            // Get air quality data
            val aqi = main?.optInt("aqi") ?: 0
            val aqiText = convertAqiToText(aqi)
            val co = components?.optDouble("co") ?: 0.0
            val no2 = components?.optDouble("no2") ?: 0.0
            val o3 = components?.optDouble("o3") ?: 0.0
            val so2 = components?.optDouble("so2") ?: 0.0

            // Get TextViews
            val aqiTextView: TextView = findViewById(R.id.aqiTextView)
            val so2TextView: TextView = findViewById(R.id.textView16)
            val no2TextView: TextView = findViewById(R.id.textView17)
            val o3TextView: TextView = findViewById(R.id.textView20)
            val coTextView: TextView = findViewById(R.id.textView21)

            // Update TextViews with air quality data
            aqiTextView.text = getString(R.string.aqi, aqiText)
            so2TextView.text = getString(R.string.so2, so2)
            no2TextView.text = getString(R.string.no2, no2)
            o3TextView.text = getString(R.string.o3, o3)
            coTextView.text = getString(R.string.co, co)
        } else {
            showError("No air quality data available")
        }
        lastUpdate()
    }

    // Function to convert AQI index to text
    private fun convertAqiToText(aqi: Int): String {
        return when (aqi) {
            1 -> "Good"
            2 -> "Fair"
            3 -> "Moderate"
            4 -> "Poor"
            5 -> "Very Poor"
            else -> "Unknown"
        }
    }

    // Function to get weather icon
    private fun getWeatherIcon(main: String): Int {
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

    private fun changeBackgroundImage(main: String, sunrise: Date, sunset: Date) {
        // Get current time
        val currentTime = System.currentTimeMillis()
        val background: ConstraintLayout = findViewById(R.id.mainLayout)

        if (currentTime in sunset.time..sunrise.time) {
            background.setBackgroundResource(R.drawable.black_gradient_background)
        } else {
            when (main) {
                "Snow" -> background.setBackgroundResource(R.drawable.black_gradient_background)
                "Rain", "Drizzle", "Thunderstorm", "Mist", "Smoke", "Haze", "Dust", "Fog", "Sand", "Ash", "Squall", "Tornado" -> background.setBackgroundResource(
                    R.drawable.black_gradient_background
                )

                "Clear", "Clouds" -> background.setBackgroundResource(R.drawable.blue_gradient_background)
                else -> background.setBackgroundResource(R.drawable.blue_gradient_background) // Default background
            }
        }
    }

    // Function to convert degrees to cardinal direction
    private fun convertDegreesToCardinalDirection(degrees: Double): String {
        val directions = arrayOf(
            "North",
            "North East",
            "East",
            "South East",
            "South",
            "South West",
            "West",
            "North West",
            "North"
        )
        return directions[((degrees % 360) / 45).toInt()]
    }

    // Function to show error message
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Function to save location to dataStore
    private suspend fun saveLocation(latitude: Double, longitude: Double) {
        dataStore.edit { locations ->
            val currentLat = locations[dataStoreLat] ?: 51.5072
            val currentLng = locations[dataStoreLng] ?: -0.1276
            if (currentLat != latitude || currentLng != longitude) {
                locations[dataStoreLat] = latitude
                locations[dataStoreLng] = longitude
            }
        }

    }
}