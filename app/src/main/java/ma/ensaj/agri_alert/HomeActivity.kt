package ma.ensaj.agri_alert

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import ma.ensaj.agri_alert.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide the ActionBar
        supportActionBar?.hide()

        // Set the status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_dark)

        // Inflate the layout using ViewBinding
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up OnClickListener for the chatbot icon
        binding.ivChatbot.setOnClickListener {
            val intent = Intent(this, ChatBotActivity::class.java) // Use 'this' instead of 'requireContext'
            startActivity(intent)
        }
        binding.ivProfile.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }


        // Explicitly find the NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment?

        if (navHostFragment != null) {
            navController = navHostFragment.navController

            // Setup BottomNavigationView with NavController
            binding.bottomNavigation.setupWithNavController(navController)
        } else {
            Log.e("HomeActivity", "NavHostFragment is null")
            Toast.makeText(this, "Navigation setup failed", Toast.LENGTH_SHORT).show()
        }
    }
}
