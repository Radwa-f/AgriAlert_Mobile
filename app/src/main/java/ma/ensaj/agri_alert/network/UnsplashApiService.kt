package ma.ensaj.agri_alert.network
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface UnsplashApiService {
    @GET("photos/random")
    fun getRandomPhoto(
        @Query("client_id") clientId: String,
        @Query("query") query: String
    ): Call<UnsplashPhotoResponse>
}

data class UnsplashPhotoResponse(val urls: Urls)
data class Urls(val regular: String)
