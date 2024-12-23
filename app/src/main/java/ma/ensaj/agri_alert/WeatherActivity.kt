package ma.ensaj.agri_alert

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensaj.agri_alert.databinding.ActivityWeatherBinding
import ma.ensaj.agri_alert.model.Daily
import ma.ensaj.agri_alert.model.WeatherResponse
import ma.ensaj.agri_alert.network.WeatherApi

class WeatherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherBinding
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)


        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_dark)

        // Get latitude and longitude from Intent
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)

        if (latitude != 0.0 && longitude != 0.0) {
            fetchWeatherData(latitude, longitude)
            fetchCityName(latitude, longitude)
        } else {
            Log.e("WeatherActivity", "Latitude or Longitude is missing")
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
                        hourly = "precipitation",
                        timezone = "auto"
                    )
                }
                updateUIWithWeatherData(weatherResponse)
            } catch (e: Exception) {
                Log.e("WeatherAPI", "Error fetching weather data: ${e.message}")
            }
        }
    }

    private fun updateUIWithWeatherData(weatherResponse: WeatherResponse) {
        val todayTemperature = weatherResponse.daily.temperatureMax[0]
        val todayMinTemperature = weatherResponse.daily.temperatureMin[0]
        val todayPrecipitation = weatherResponse.daily.precipitationSum[0]
        val todayCondition = when {
            todayPrecipitation > 0.0 -> "Rainy"
            todayTemperature > 25 -> "Sunny"
            else -> "Cloudy"
        }

        // Update today's weather card
        binding.tvTodayTemperature.text = "$todayTemperature°C"
        binding.tvTodayWeatherCondition.text = todayCondition

        // Set the weather icon based on today's condition
        val iconRes = when (todayCondition) {
            "Rainy" -> R.drawable.ic_rainy
            "Sunny" -> R.drawable.ic_sunny
            "Cloudy" -> R.drawable.ic_cloudy
            else -> R.drawable.ic_weather_placeholder
        }
        binding.weatherIcon.setImageResource(iconRes)

        // Setup RecyclerView for other days
        val otherDays = Daily(
            temperatureMax = weatherResponse.daily.temperatureMax.drop(1),
            temperatureMin = weatherResponse.daily.temperatureMin.drop(1),
            precipitationSum = weatherResponse.daily.precipitationSum.drop(1)
        )
        binding.recyclerViewWeather.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewWeather.adapter = WeatherAdapter(otherDays)
    }

    private fun fetchCityName(latitude: Double, longitude: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(this@WeatherActivity)
                val addressList = geocoder.getFromLocation(latitude, longitude, 1)
                val cityName = addressList?.get(0)?.locality ?: "Unknown City"
                withContext(Dispatchers.Main) {
                    binding.tvCityName.text = cityName
                }
            } catch (e: Exception) {
                Log.e("Geocoder", "Error fetching city name: ${e.message}")
                withContext(Dispatchers.Main) {
                    binding.tvCityName.text = "Unknown City"
                }
            }
        }
    }
}
