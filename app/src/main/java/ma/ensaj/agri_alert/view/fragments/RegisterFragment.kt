package ma.ensaj.agri_alert.view.fragments

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import ma.ensaj.agri_alert.R
import ma.ensaj.agri_alert.model.Location
import ma.ensaj.agri_alert.model.RegistrationRequest
import ma.ensaj.agri_alert.network.RetrofitClient
import ma.ensaj.agri_alert.util.SharedPreferencesHelper
import ma.ensaj.agri_alert.view.viewmodel.UserViewModel
import androidx.appcompat.app.AppCompatActivity

class RegisterFragment : Fragment() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var latitude: Double? = null
    private var longitude: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (checkLocationPermission()) {
            fetchLocation()
        }

        val etFirstName = view.findViewById<EditText>(R.id.et_first_name)
        val etLastName = view.findViewById<EditText>(R.id.et_last_name)
        val etEmail = view.findViewById<EditText>(R.id.et_email)
        val etPassword = view.findViewById<EditText>(R.id.et_password)
        val etRepassword = view.findViewById<EditText>(R.id.et_repassword)
        val etPhone = view.findViewById<EditText>(R.id.et_phone)
        val btnRegister = view.findViewById<Button>(R.id.btn_register)

        btnRegister.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val phoneNumber = etPhone.text.toString().trim()

            when {
                firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || phoneNumber.isEmpty() -> {
                    showToast("Please fill all fields")
                }
                latitude == null || longitude == null -> {
                    showToast("Please enable location to proceed")
                }
                else -> {
                    val location = Location(latitude!!, longitude!!)
                    val request = RegistrationRequest(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        password = password,
                        phoneNumber = phoneNumber,
                        location = location,
                        crops = listOf("Wheat", "Corn") // Example crops
                    )
                    registerUser(request)
                }
            }
        }
    }

    private fun registerUser(request: RegistrationRequest) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.registerUser(request)
                if (response.isSuccessful) {
                    showToast("Registration successful")
                    clearInputs()
                    SharedPreferencesHelper.saveLocation(requireContext(), request.location.latitude, request.location.longitude)
                } else {
                    showToast("Registration failed: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                showToast("An error occurred: ${e.message}")
            }
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    showToast("Location fetched: $latitude, $longitude")
                } else {
                    showToast("Unable to fetch location. Please try again.")
                }
            }.addOnFailureListener {
                showToast("Failed to fetch location: ${it.message}")
            }
        } else {
            showToast("Location permission not granted.")
        }
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

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun clearInputs() {
        view?.findViewById<EditText>(R.id.et_first_name)?.text?.clear()
        view?.findViewById<EditText>(R.id.et_last_name)?.text?.clear()
        view?.findViewById<EditText>(R.id.et_email)?.text?.clear()
        view?.findViewById<EditText>(R.id.et_password)?.text?.clear()
        view?.findViewById<EditText>(R.id.et_repassword)?.text?.clear()
        view?.findViewById<EditText>(R.id.et_phone)?.text?.clear()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
