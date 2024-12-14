package ma.ensaj.agri_alert.network

import retrofit2.http.Body
import retrofit2.http.POST

data class ChatRequest(val query: String)
data class ChatResponse(val response: String)

interface ChatBotService {
    @POST("/chat")
    suspend fun getChatResponse(@Body request: ChatRequest): ChatResponse
}
