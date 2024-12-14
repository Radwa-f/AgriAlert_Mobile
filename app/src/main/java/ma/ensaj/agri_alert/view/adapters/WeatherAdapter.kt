package ma.ensaj.agri_alert

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ma.ensaj.agri_alert.databinding.ItemWeatherBinding
import ma.ensaj.agri_alert.model.Daily

class WeatherAdapter(private val daily: Daily) :
    RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>() {

    inner class WeatherViewHolder(private val binding: ItemWeatherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val maxTemp = daily.temperatureMax[position]
            val minTemp = daily.temperatureMin[position]
            val precipitation = daily.precipitationSum[position]

            val condition = when {
                precipitation > 0.0 -> "Rainy"
                maxTemp > 25 -> "Sunny"
                else -> "Cloudy"
            }

            binding.tvDay.text = "Day ${position + 2}" // Example day labels
            binding.tvTemperatureRange.text = "${minTemp}°C - ${maxTemp}°C"
            binding.ivWeatherIcon.setImageResource(
                when (condition) {
                    "Sunny" -> R.drawable.ic_sunny
                    "Rainy" -> R.drawable.ic_rainy
                    else -> R.drawable.ic_cloudy
                }
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val binding = ItemWeatherBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WeatherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = daily.temperatureMax.size
}
