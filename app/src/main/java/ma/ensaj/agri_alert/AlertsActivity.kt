package ma.ensaj.agri_alert

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ma.ensaj.agri_alert.databinding.ActivityAlertsBinding
import ma.ensaj.agri_alert.model.Alert
import ma.ensaj.agri_alert.view.adapters.AlertsAdapter

class AlertsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlertsBinding
    private val alertsList = mutableListOf<Alert>() // Replace this with data from your API

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up RecyclerView
        val adapter = AlertsAdapter(alertsList)
        binding.rvAllAlerts.layoutManager = LinearLayoutManager(this)
        binding.rvAllAlerts.adapter = adapter

        // Dummy data for testing
        alertsList.add(Alert("Frost Warning", "Low Temperature", "Protect your crops from frost.","HIGH"))
        alertsList.add(Alert("Heavy Rain", "High Precipitation", "Prepare drainage for heavy rain.","HIGH"))
        alertsList.add(Alert("Pest Alert", "Pest Activity", "Use pesticide for pest control.","HIGH"))
        adapter.notifyDataSetChanged()
    }
}
