package ma.ensaj.agri_alert.network

import ma.ensaj.agri_alert.model.AuthResponse
import ma.ensaj.agri_alert.model.CropAnalysisResponse
import ma.ensaj.agri_alert.model.LoginRequest
import ma.ensaj.agri_alert.model.RegistrationRequest
import ma.ensaj.agri_alert.model.UserProfile
import ma.ensaj.agri_alert.model.WeatherAnalysisRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface UserService {

    @POST("api/v1/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<String>


    @POST("api/v1/registration")
    suspend fun registerUser(@Body request: RegistrationRequest): Response<Void>

    @POST("api/crops/weather-analysis")
    suspend fun getWeatherAnalysis(@Body request: WeatherAnalysisRequest): Response<CropAnalysisResponse>

    @GET("api/v1/user/profile")
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<UserProfile>

}
