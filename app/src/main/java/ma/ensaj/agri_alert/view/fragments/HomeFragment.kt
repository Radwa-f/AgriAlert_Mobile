package ma.ensaj.agri_alert.view.fragments

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensaj.agri_alert.AlertsActivity
import ma.ensaj.agri_alert.R
import ma.ensaj.agri_alert.databinding.FragmentHomeBinding
import ma.ensaj.agri_alert.model.WeatherResponse
import ma.ensaj.agri_alert.network.WeatherApi
import ma.ensaj.agri_alert.ChatBotActivity
import ma.ensaj.agri_alert.WeatherActivity
import ma.ensaj.agri_alert.model.Alert
import ma.ensaj.agri_alert.view.adapters.AlertsAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double? = null
    private var longitude: Double? = null

    private var weatherResponse: WeatherResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Set up OnClickListener for chatbot icon
        binding.ivChatbot.setOnClickListener {
            val intent = Intent(requireContext(), ChatBotActivity::class.java)
            startActivity(intent)
        }

        // Navigate to WeatherActivity
        binding.weatherCard.setOnClickListener {
            val intent = Intent(requireContext(), WeatherActivity::class.java)
            intent.putExtra("latitude", latitude)
            intent.putExtra("longitude", longitude)
            startActivity(intent)
        }

        // Dummy alerts for RecyclerView
        val alerts = listOf(
            Alert("Frost Warning", "Low Temperature", "Protect your crops from frost."),
            Alert("Heavy Rain", "High Precipitation", "Prepare drainage for heavy rain."),
            Alert("Pest Alert", "Pest Activity", "Use pesticide for pest control."),
            Alert("Drought Risk", "High Temperature", "Ensure irrigation system is ready."),
            Alert("Windstorm", "High Wind Speeds", "Secure your equipment.")
        )

        val adapter = AlertsAdapter(alerts)
        binding.rvDailyInsights.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDailyInsights.adapter = adapter

        // Navigate to AlertsActivity
        binding.rvDailyInsights.setOnClickListener {
            val intent = Intent(requireContext(), AlertsActivity::class.java)
            startActivity(intent)
        }

        // Check for location permission and fetch location
        if (checkLocationPermission()) {
            fetchLocation()
        } else {
            Log.e("Position", "Location permission not granted.")
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null && location.latitude != 0.0 && location.longitude != 0.0) {
                    latitude = location.latitude
                    longitude = location.longitude
                    Log.d("Position", "Fetched location: Latitude = $latitude, Longitude = $longitude")
                    fetchWeatherData(latitude!!, longitude!!)
                    fetchCityName(latitude!!, longitude!!)
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
                val response = withContext(Dispatchers.IO) {
                    WeatherApi.retrofitService.getWeather(
                        latitude = latitude,
                        longitude = longitude,
                        daily = "temperature_2m_max,temperature_2m_min,precipitation_sum",
                        timezone = "auto"
                    )
                }
                Log.d("WeatherAPI", "Response: $response")

                weatherResponse = response
                updateWeatherCard(response)
            } catch (e: Exception) {
                Log.e("WeatherAPI", "Error fetching weather data: ${e.message}")
            }
        }
    }

    private fun updateWeatherCard(weatherResponse: WeatherResponse) {
        val currentTemperature = weatherResponse.daily.temperatureMax[0]
        val precipitation = weatherResponse.daily.precipitationSum[0]
        val weatherCondition = when {
            precipitation > 0.0 -> "Rainy"
            currentTemperature > 25 -> "Sunny"
            else -> "Cloudy"
        }

        binding.tvWeatherCondition.text = weatherCondition
        binding.tvTemperatureDetails.text = "$currentTemperature°C"

        val iconRes = when (weatherCondition) {
            "Sunny" -> R.drawable.ic_sunny
            "Rainy" -> R.drawable.ic_rainy
            "Cloudy" -> R.drawable.ic_cloudy
            else -> R.drawable.ic_weather_placeholder
        }
        binding.weatherIcon.setImageResource(iconRes)
    }

    private fun checkLocationPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            false
        } else {
            true
        }
    }

    private fun fetchCityName(latitude: Double, longitude: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext())
                val addressList = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addressList.isNullOrEmpty()) {
                    val cityName = addressList[0].locality ?: "Unknown City"
                    withContext(Dispatchers.Main) {
                        binding.tvForecastTitle.text = cityName
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        binding.tvForecastTitle.text = "Unknown City"
                    }
                }
            } catch (e: Exception) {
                Log.e("Geocoder", "Error fetching city name: ${e.message}")
                withContext(Dispatchers.Main) {
                    binding.tvForecastTitle.text = "Unknown City"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
