package ma.ensaj.agri_alert.view.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ma.ensaj.agri_alert.databinding.FragmentNewsBinding
import ma.ensaj.agri_alert.model.NewsItem
import ma.ensaj.agri_alert.network.RetrofitInstance
import ma.ensaj.agri_alert.view.adapters.NewsAdapter

class NewsFragment : Fragment() {
    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!

    private lateinit var newsAdapter: NewsAdapter
    private var originalNewsList = listOf<NewsItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and Adapter
        newsAdapter = NewsAdapter(mutableListOf()) // Initialize with an empty list
        binding.newsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.newsRecyclerView.adapter = newsAdapter

        setupSearchBar()
        fetchNewsData()
    }

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterNews(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchNewsData() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getNews(
                        text = "agriculture",
                        sourceCountry = "ma",
                        number = 10,
                        apiKey = "9f7f2f2a7052471f9fc6d6e478b38a77"
                    )
                }

                if (response.news.isNotEmpty()) {
                    originalNewsList = response.news.sortedWith(compareBy<NewsItem> {
                        // Prioritize titles starting with "A"
                        it.title?.startsWith("A", ignoreCase = true) == false
                    }.thenBy {
                        // Sort alphabetically as a fallback
                        it.title
                    })
                    setupRecyclerView(originalNewsList)
                } else {
                    Log.e("NewsFragment", "No news data available.")
                }
            } catch (e: Exception) {
                Log.e("NewsFragment", "Error fetching news", e)
            }
        }
    }

    private fun filterNews(query: String) {
        if (::newsAdapter.isInitialized) { // Ensure the adapter is initialized
            val filteredList = originalNewsList.filter {
                it.title?.contains(query, ignoreCase = true) == true
            }.sortedWith(compareBy<NewsItem> {
                // Prioritize titles starting with "A"
                it.title?.startsWith("A", ignoreCase = true) == false
            }.thenBy {
                // Sort alphabetically as a fallback
                it.title
            })
            newsAdapter.updateList(filteredList)
        }
    }


    private fun setupRecyclerView(newsList: List<NewsItem>) {
        newsAdapter.updateList(newsList)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

