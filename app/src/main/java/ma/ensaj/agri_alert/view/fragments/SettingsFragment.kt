package ma.ensaj.agri_alert.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import ma.ensaj.agri_alert.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("SettingsFragment", "onViewCreated called")

        // Edit Profile Click Listener
        binding.tvEditProfile.setOnClickListener {
            Toast.makeText(context, "Edit Profile Clicked", Toast.LENGTH_SHORT).show()
        }

        // Weather Updates Toggle
        binding.switchWeatherUpdates.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(context, "Weather Updates Enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Weather Updates Disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // Logout Button Click
        binding.btnLogout.setOnClickListener {
            Toast.makeText(context, "Logged Out", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
