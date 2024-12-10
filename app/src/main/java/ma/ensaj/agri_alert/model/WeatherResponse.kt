package ma.ensaj.agri_alert.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("daily") val daily: Daily
)

data class Daily(
    @SerializedName("temperature_2m_max") val temperatureMax: List<Double>,
    @SerializedName("temperature_2m_min") val temperatureMin: List<Double>,
    @SerializedName("precipitation_sum") val precipitationSum: List<Double>
)