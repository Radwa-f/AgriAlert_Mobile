package ma.ensaj.agri_alert.view.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import ma.ensaj.agri_alert.R
import ma.ensaj.agri_alert.HomeActivity
import ma.ensaj.agri_alert.model.LoginRequest
import ma.ensaj.agri_alert.network.RetrofitClient
import ma.ensaj.agri_alert.util.SharedPreferencesHelper
import ma.ensaj.agri_alert.view.viewmodel.UserViewModel

class LoginFragment : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var latitude: Double? = null
    private var longitude: Double? = null
    private lateinit var userViewModel: UserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (checkLocationPermission()) {
            fetchLocation()
        }
        val etEmail = view.findViewById<EditText>(R.id.et_email)
        val etPassword = view.findViewById<EditText>(R.id.et_password)
        val btnLogin = view.findViewById<Button>(R.id.btn_login)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.loginResult.collect { result ->
                    result?.let {
                        if (it.isSuccess) {
                            showToast("Login Success")
                            clearInputs()
                            navigateToActivity()
                        } else {
                            showToast("Login failed")
                        }
                    }
                }
            }
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            when {
                email.isEmpty() || password.isEmpty() -> {
                    showToast("Please fill all fields")
                }
                else -> {
                    loginUser(email, password) // Call the loginUser function directly
                }
            }
        }

    }

    private fun navigateToActivity() {
        val intent = Intent(requireContext(), HomeActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun clearInputs() {
        view?.findViewById<EditText>(R.id.et_email)?.text?.clear()
        view?.findViewById<EditText>(R.id.et_password)?.text?.clear()
    }

    private fun loginUser(email: String, password: String) {
        lifecycleScope.launch {
            try {
                val request = LoginRequest(email, password)
                val response = RetrofitClient.instance.loginUser(request)
                if (response.isSuccessful) {
                    val token = response.body() // Get the plain JWT token
                    if (token != null) {
                        saveToken(token) // Save the token for future use
                        showToast("Login successful, Token saved")
                        context?.let { SharedPreferencesHelper.saveToken(it, token) } // Save the token

                        navigateToActivity()
                    } else {
                        showToast("Login failed: Empty response")
                    }
                } else {
                    showToast("Login failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("LoginFragment", "Error during login", e)
                showToast("An error occurred: ${e.message}")
            }
        }
    }


    private fun saveToken(token: String) {
        val sharedPreferences = requireContext().getSharedPreferences("MyAppPreferences", AppCompatActivity.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("JWT_TOKEN", token)
        editor.apply()
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null && location.latitude != 0.0 && location.longitude != 0.0) {
                    latitude = location.latitude
                    longitude = location.longitude
                    saveLocation(latitude!!, longitude!!)
                    Log.d("Position", "Fetched location: Latitude = $latitude, Longitude = $longitude")
                } else {
                    Log.d("Position", "Unable to fetch a valid location. Location object: $location")
                    showToast("Unable to fetch location. Please ensure GPS is enabled.")
                }
            }.addOnFailureListener {
                Log.e("Position", "Failed to fetch location: ${it.message}")
                showToast("Failed to fetch location: ${it.message}")
            }
        } else {
            Log.d("Position", "Location permission not granted.")
            showToast("Location permission not granted. Please enable location.")
        }
    }

    private fun saveLocation(latitude: Double, longitude: Double) {
        val sharedPreferences = requireContext().getSharedPreferences("MyAppPreferences", AppCompatActivity.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("USER_LATITUDE", latitude.toFloat())
        editor.putFloat("USER_LONGITUDE", longitude.toFloat())
        editor.apply()
        Log.d("Position", "Location saved: Latitude = $latitude, Longitude = $longitude")
    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun checkLocationPermission(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            false
        } else {
            true
        }
    }


}
