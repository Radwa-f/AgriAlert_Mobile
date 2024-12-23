package ma.ensaj.agri_alert.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("daily") val daily: Daily,
    @SerializedName("hourly") val hourly: Hourly
)

data class Daily(
    @SerializedName("temperature_2m_max") val temperatureMax: List<Double>,
    @SerializedName("temperature_2m_min") val temperatureMin: List<Double>,
    @SerializedName("precipitation_sum") val precipitationSum: List<Double>
)

data class Hourly(
    @SerializedName("precipitation") val precipitation: List<Double>, // Hourly precipitation
    @SerializedName("time") val time: List<String> // Corresponding timestamps
)