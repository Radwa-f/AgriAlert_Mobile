package ma.ensaj.agri_alert.model

data class WeatherAnalysisRequest(
    val maxTemp: Double,
    val minTemp: Double,
    val maxRain: Double,
    val minRain: Double,
    val cropNames: List<String>
)
