package ma.ensaj.agri_alert

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensaj.agri_alert.databinding.ActivityHomeBinding
import ma.ensaj.agri_alert.model.WeatherResponse
import ma.ensaj.agri_alert.network.WeatherApi
import ma.ensaj.agri_alert.R

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var latitude: Double? = null
    private var longitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check for location permission and fetch location
        if (checkLocationPermission()) {
            fetchLocation()
        } else {
            Log.e("Position", "Location permission not granted.")
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null && location.latitude != 0.0 && location.longitude != 0.0) {
                    latitude = location.latitude
                    longitude = location.longitude
                    Log.d("Position", "Fetched location: Latitude = $latitude, Longitude = $longitude")
                    fetchWeatherData(latitude!!, longitude!!)
                } else {
                    Log.d("Position", "Unable to fetch a valid location.")
                }
            }.addOnFailureListener {
                Log.e("Position", "Failed to fetch location: ${it.message}")
            }
        } else {
            Log.e("Position", "Location permission not granted.")
        }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        lifecycleScope.launch {
            try {
                val weatherResponse = withContext(Dispatchers.IO) {
                    WeatherApi.retrofitService.getWeather(
                        latitude = latitude,
                        longitude = longitude,
                        daily = "temperature_2m_max,temperature_2m_min,precipitation_sum",
                        timezone = "auto"
                    )
                }
                Log.d("WeatherAPI", "Response: $weatherResponse")

                updateWeatherCard(weatherResponse)
            } catch (e: Exception) {
                Log.e("WeatherAPI", "Error fetching weather data: ${e.message}")
            }
        }
    }

    private fun updateWeatherCard(weatherResponse: WeatherResponse) {
        val currentTemperature = weatherResponse.daily.temperatureMax[0]
        val weatherCondition = "Sunny" // Replace with actual logic if available from API

        binding.tvForecastTitle.text = "El Jadida"
        binding.tvForecastDetails.text = "$weatherCondition, $currentTemperature°C"

        // Update weather icon
        val iconRes = when {
            weatherCondition.contains("Sunny", ignoreCase = true) -> R.drawable.ic_sunny
            weatherCondition.contains("Rain", ignoreCase = true) -> R.drawable.ic_rainy
            else -> R.drawable.ic_cloudy
        }
        binding.weatherIcon.setImageResource(iconRes)
    }

    private fun checkLocationPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            false
        } else {
            true
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
