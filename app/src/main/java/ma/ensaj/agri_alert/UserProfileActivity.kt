package ma.ensaj.agri_alert

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensaj.agri_alert.databinding.ActivityUserProfileBinding
import ma.ensaj.agri_alert.model.UserProfile
import ma.ensaj.agri_alert.network.RetrofitClient
import ma.ensaj.agri_alert.util.SharedPreferencesHelper
import java.util.Locale

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding

    // Class-level variable to store the user profile
    private var userProfile: UserProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.my_dark)

        fetchUserProfile()

        // Setup update location button click
        binding.btnEditLocation.setOnClickListener {
            val profile = userProfile
            if (profile != null) {
                val intent = Intent(this, LocationActivity::class.java)
                intent.putExtra("latitude", profile.location?.latitude ?: 0.0)
                intent.putExtra("longitude", profile.location?.longitude ?: 0.0)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Profile not loaded yet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUserProfile() {
        val token = SharedPreferencesHelper.getToken(this)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Authentication token is missing", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getUserProfile("Bearer $token")
                }

                if (response.isSuccessful) {
                    userProfile = response.body() // Save the user profile to the class-level variable
                    if (userProfile != null) {
                        displayUserProfile(userProfile!!)
                    } else {
                        Toast.makeText(this@UserProfileActivity, "Failed to fetch profile", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("UserProfile", "Error: ${response.errorBody()?.string()}")
                    Toast.makeText(this@UserProfileActivity, "Error fetching profile", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("UserProfile", "Exception: ${e.message}")
                Toast.makeText(this@UserProfileActivity, "An error occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayUserProfile(userProfile: UserProfile) {
        binding.tvUserFullName.text = "${userProfile.firstName} ${userProfile.lastName}"
        binding.tvUserEmail.text = userProfile.email
        binding.tvUserPhone.text = userProfile.phoneNumber
        binding.tvUserCrops.text = userProfile.crops.joinToString(", ")

        // Display location details
        val latitude = userProfile.location?.latitude ?: 0.0
        val longitude = userProfile.location?.longitude ?: 0.0

        binding.tvUserLocation.text = "Lat: $latitude, Long: $longitude"

        // Fetch and display city name
        fetchCityName(latitude, longitude)
    }

    private fun fetchCityName(latitude: Double, longitude: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(this@UserProfileActivity, Locale.getDefault())
                val addressList = geocoder.getFromLocation(latitude, longitude, 1)
                val cityName = if (!addressList.isNullOrEmpty()) {
                    addressList[0].locality ?: "Unknown City"
                } else {
                    "Unknown City"
                }
                withContext(Dispatchers.Main) {
                    binding.tvUserLocationCity.text = cityName
                }
            } catch (e: Exception) {
                Log.e("Geocoder", "Error fetching city name: ${e.message}")
                withContext(Dispatchers.Main) {
                    binding.tvUserLocationCity.text = "Unknown City"
                }
            }
        }
    }
}
