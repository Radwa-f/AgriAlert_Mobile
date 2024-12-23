package ma.ensaj.agri_alert.network

import ma.ensaj.agri_alert.model.WeatherResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherService {
    @GET("/v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String,
        @Query("hourly") hourly: String,
        @Query("timezone") timezone: String
    ): WeatherResponse
}
