package ma.ensaj.agri_alert.view.adapters

import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ma.ensaj.agri_alert.R
import ma.ensaj.agri_alert.databinding.NewsItemBinding
import ma.ensaj.agri_alert.model.NewsItem

class NewsAdapter(private val newsList: MutableList<NewsItem>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {



    class NewsViewHolder(private val binding: NewsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(newsItem: NewsItem) {
            // Handle nullable fields
            binding.newsTitle.text = newsItem.title ?: "No Title"
            binding.newsDescription.text = newsItem.text?.let { fullText ->
                val firstStopIndex = fullText.indexOf('.')
                val truncatedText = if (firstStopIndex != -1) {
                    fullText.substring(0, firstStopIndex + 1) // Include the full stop
                } else {
                    fullText // If no full stop, display the whole text
                }

                val spannableString = SpannableString("$truncatedText Read more...")
                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        newsItem.url?.let { url ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            widget.context.startActivity(intent)
                        }
                    }
                }

                spannableString.setSpan(
                    clickableSpan,
                    truncatedText.length + 1, // Start of "Read more..."
                    spannableString.length,  // End of the string
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannableString
            } ?: "No Description"

            binding.newsDescription.movementMethod = android.text.method.LinkMovementMethod.getInstance()

            Glide.with(binding.newsImage.context)
                .load(newsItem.image?: R.drawable.ic_news_prev)
                .placeholder(R.drawable.ic_news_2) // Placeholder image
                .into(binding.newsImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = NewsItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(newsList[position])
    }

    override fun getItemCount(): Int = newsList.size
    fun updateList(newList: List<NewsItem>) {
        newsList.clear() // Built-in clear function
        newsList.addAll(newList) // Add new items
        notifyDataSetChanged() // Notify the adapter of changes
    }



}


