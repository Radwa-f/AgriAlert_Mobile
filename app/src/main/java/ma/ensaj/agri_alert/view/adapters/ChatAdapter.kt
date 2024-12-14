package ma.ensaj.agri_alert

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ma.ensaj.agri_alert.databinding.ItemChatMessageBinding

class ChatAdapter(private val messages: List<Pair<String, String>>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(val binding: ItemChatMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        if (message.first == "User") {
            holder.binding.tvUserMessage.text = message.second
            holder.binding.tvUserMessage.visibility = View.VISIBLE
            holder.binding.tvBotMessage.visibility = View.GONE
        } else {
            holder.binding.tvBotMessage.text = message.second
            holder.binding.tvBotMessage.visibility = View.VISIBLE
            holder.binding.tvUserMessage.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = messages.size
}
