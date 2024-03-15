package com.example.kotlin_weather_app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
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

class MainActivity : AppCompatActivity() {
    private val _permissionRequestAccessFineLocation = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // check permission is granted or not
        getLocationPermission()

        // get current location
        getLocation();

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
}