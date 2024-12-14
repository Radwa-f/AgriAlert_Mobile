package ma.ensaj.agri_alert

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensaj.agri_alert.databinding.ActivityChatBotBinding
import ma.ensaj.agri_alert.network.ChatBotApi
import ma.ensaj.agri_alert.network.ChatRequest

import ma.ensaj.agri_alert.network.ChatResponse


class ChatBotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBotBinding
    private val chatMessages = mutableListOf<Pair<String, String>>() // User and Bot messages

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.nav)


        // Set up RecyclerView
        val adapter = ChatAdapter(chatMessages)
        binding.rvChatConversation.layoutManager = LinearLayoutManager(this)
        binding.rvChatConversation.adapter = adapter

        // Handle Send Button Click
        binding.btnSendMessage.setOnClickListener {
            val userMessage = binding.etMessageInput.text.toString()
            if (userMessage.isNotBlank()) {
                sendMessageToChatBot(userMessage, adapter)
                binding.etMessageInput.text.clear()
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMessageToChatBot(userMessage: String, adapter: ChatAdapter) {
        // Add user message to the chat
        chatMessages.add(Pair("User", userMessage))
        adapter.notifyItemInserted(chatMessages.size - 1)
        binding.rvChatConversation.scrollToPosition(chatMessages.size - 1)

        // Call the chatbot API
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ChatBotApi.chatbotService.getChatResponse(ChatRequest(userMessage))
                }

                // Add chatbot response to the chat
                chatMessages.add(Pair("Bot", response.response))
                adapter.notifyItemInserted(chatMessages.size - 1)
                binding.rvChatConversation.scrollToPosition(chatMessages.size - 1)
            } catch (e: Exception) {
                Log.e("ChatBotActivity", "Error calling chatbot API: ${e.message}")
                Toast.makeText(this@ChatBotActivity, "Failed to connect to chatbot", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
