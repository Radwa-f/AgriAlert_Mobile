package ma.ensaj.agri_alert.network

import ma.ensaj.agri_alert.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("search-news")
    suspend fun getNews(
        @Query("text") text: String,
        @Query("source-country") sourceCountry: String,
        @Query("number") number: Int,
        @Query("api-key") apiKey: String
    ): NewsResponse


}
