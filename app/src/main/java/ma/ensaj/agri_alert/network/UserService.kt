package ma.ensaj.agri_alert.network

import ma.ensaj.agri_alert.model.AuthResponse
import ma.ensaj.agri_alert.model.LoginRequest
import ma.ensaj.agri_alert.model.RegistrationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UserService {

    @POST("api/v1/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<String>


    @POST("api/v1/registration")
    suspend fun registerUser(@Body request: RegistrationRequest): Response<Void>
}
