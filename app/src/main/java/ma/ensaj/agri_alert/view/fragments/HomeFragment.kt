package ma.ensaj.agri_alert.view.fragments

import android.content.Intent
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensaj.agri_alert.AlertsActivity
import ma.ensaj.agri_alert.R
import ma.ensaj.agri_alert.databinding.FragmentHomeBinding
import ma.ensaj.agri_alert.model.WeatherResponse
import ma.ensaj.agri_alert.network.WeatherApi
import ma.ensaj.agri_alert.ChatBotActivity
import ma.ensaj.agri_alert.CropsDetailsActivity
import ma.ensaj.agri_alert.WeatherActivity
import ma.ensaj.agri_alert.model.Alert
import ma.ensaj.agri_alert.model.Crop
import ma.ensaj.agri_alert.model.WeatherAnalysisRequest
import ma.ensaj.agri_alert.network.RetrofitClient
import ma.ensaj.agri_alert.util.SharedPreferencesHelper
import ma.ensaj.agri_alert.view.adapters.AlertsAdapter
import ma.ensaj.agri_alert.view.adapters.CropsAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var maxRainfall: Double = 0.0
    private var minRainfall: Double = 0.0


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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission()
        }



        // Navigate to WeatherActivity
        binding.weatherCard.setOnClickListener {
            val intent = Intent(requireContext(), WeatherActivity::class.java)
            intent.putExtra("latitude", latitude)
            intent.putExtra("longitude", longitude)
            startActivity(intent)
        }

        // Add this to the onViewCreated method in HomeFragment
        val crops = listOf(
            Crop("Wheat", "Growing", R.drawable.ic_wheat),
            Crop("Corn", "Ready to Harvest", R.drawable.ic_corn),
            Crop("Rice", "Planting", R.drawable.ic_rice)
        )

        binding.rvCrops.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)


        fetchAndDisplayAlerts()

        binding.rvDailyInsights.setLayoutManager(
            LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )



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
                        hourly = "precipitation",
                        timezone = "auto"
                    )
                }
                Log.d("WeatherAPI", "Response: $response")

                weatherResponse = response
                updateWeatherCard(response)

                processRainfallForNextDay(response)

                fetchUserCrops()

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


    private fun fetchUserCrops() {
        val token = SharedPreferencesHelper.getToken(requireContext())
        if (token.isNullOrEmpty()) {
            Log.e("ProfileAPI", "Authorization token is missing")
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getUserProfile("Bearer $token")
                if (response.isSuccessful) {
                    val userProfile = response.body()
                    if (userProfile != null) {
                        val userCrops = userProfile.crops
                        Log.d("ProfileAPI", "Fetched Crops: $userCrops")

                        displayCrops(userCrops)
                        // Extract weather details
                        if (latitude != null && longitude != null && weatherResponse != null) {
                            val maxTemp = weatherResponse!!.daily.temperatureMax[1]
                            val minTemp = weatherResponse!!.daily.temperatureMin[1]
                            val maxRain = maxRainfall
                            val minRain = minRainfall

                            // Create Weather Analysis Request
                            val request = WeatherAnalysisRequest(
                                maxTemp = maxTemp,
                                minTemp = minTemp,
                                maxRain = maxRain,
                                minRain = minRain,
                                cropNames = userCrops
                            )

                            // Call Weather Analysis API
                            val analysisResponse = RetrofitClient.instance.getWeatherAnalysis(request)
                            if (analysisResponse.isSuccessful) {
                                val cropAnalysisData = analysisResponse.body()
                                Log.d("WeatherAPI", "Weather Analysis Response: $cropAnalysisData")

                                // Save the analysis response
                                SharedPreferencesHelper.saveCropAnalysis(requireContext(), cropAnalysisData)


                            } else {
                                Log.e("WeatherAPI", "Error fetching analysis: ${analysisResponse.errorBody()?.string()}")
                            }
                        }
                    }
                } else {
                    Log.e("ProfileAPI", "Error fetching profile: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("ProfileAPI", "Exception occurred: ${e.message}")
            }
        }
    }

    private fun displayCrops(userCrops: List<String>) {
        if (userCrops.isNotEmpty()) {
            // Map userCrops to a list of Crop objects (you can customize this)
            val crops = userCrops.map { cropName ->
                Crop(
                    name = cropName,
                    status = "Unknown", // Placeholder status, update if backend provides it
                    imageRes = R.drawable.ic_crops // Placeholder image
                )
            }

            Log.d("Crops", "Displaying crops in RecyclerView: $crops")

            // Update the RecyclerView
            val cropsAdapter = CropsAdapter(crops, requireContext())

            binding.rvCrops.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            binding.rvCrops.adapter = cropsAdapter
        } else {
            Log.d("Crops", "No crops found for the user.")
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Request the permission
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                false
            } else {
                // Permission already granted
                true
            }
        } else {
            // For devices below Android 13, no permission is needed
            true
        }
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Log.d("Permissions", "Notification permission granted")
            } else {
                Log.e("Permissions", "Notification permission denied")
            }
        }

    private fun processRainfallForNextDay(weatherResponse: WeatherResponse) {
        val hourlyPrecipitation = weatherResponse.hourly.precipitation
        val hourlyTime = weatherResponse.hourly.time

        // Get the timestamps for the next day
        val nextDayStartIndex = hourlyTime.indexOfFirst { it.contains("T00:00") } + 24
        val nextDayEndIndex = nextDayStartIndex + 23 // 24 hours in a day

        if (nextDayStartIndex in hourlyPrecipitation.indices && nextDayEndIndex in hourlyPrecipitation.indices) {
            val nextDayPrecipitation = hourlyPrecipitation.subList(nextDayStartIndex, nextDayEndIndex + 1)

            maxRainfall = nextDayPrecipitation.maxOrNull() ?: 0.0
            minRainfall = nextDayPrecipitation.minOrNull() ?: 0.0

            Log.d("WeatherAPI", "Next Day Rainfall - Max: $maxRainfall, Min: $minRainfall")

            // You can now use maxRainfall and minRainfall for the weather analysis
        } else {
            Log.e("WeatherAPI", "Unable to calculate rainfall for the next day.")
        }
    }

    private fun fetchAndDisplayAlerts() {
        Log.d("Alerts", "Fetching alerts from SharedPreferences")

        // Retrieve CropAnalysisResponse from SharedPreferences
        val cropAnalysisData = SharedPreferencesHelper.getCropAnalysis(requireContext())

        if (cropAnalysisData != null) {
            Log.d("Alerts", "CropAnalysisResponse retrieved: $cropAnalysisData")

            // Extract all alerts from all crop analyses
            val alerts = cropAnalysisData.cropAnalyses.values.flatMap { it.alerts }

            // Log the number and details of alerts
            Log.d("Alerts", "Number of alerts extracted: ${alerts.size}")
            alerts.forEachIndexed { index, alert ->
                Log.d("Alerts", "Alert $index: $alert")
            }

            if (alerts.isNotEmpty()) {
                Log.d("Alerts", "Displaying alerts in RecyclerView")

                // Update RecyclerView with alerts
                val adapter = AlertsAdapter(alerts)
                binding.rvDailyInsights.layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                binding.rvDailyInsights.adapter = adapter
            } else {
                Log.d("Alerts", "No alerts found to display")
            }
        } else {
            Log.d("Alerts", "CropAnalysisResponse is null. No alerts to display.")
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
