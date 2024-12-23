package ma.ensaj.agri_alert.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitUnsplash {
    private const val BASE_URL = "https://api.unsplash.com/"

    val api: UnsplashApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UnsplashApiService::class.java)
    }
}
