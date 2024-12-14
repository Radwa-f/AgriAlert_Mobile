package ma.ensaj.agri_alert.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ma.ensaj.agri_alert.R
import ma.ensaj.agri_alert.adapters.NewsAdapter
import ma.ensaj.agri_alert.databinding.FragmentNewsBinding
import ma.ensaj.agri_alert.model.NewsItem

class NewsFragment : Fragment() {
    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!

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

        val sampleNews = listOf(
            NewsItem("Agriculture Boosts Economy", "Agriculture sector grows 15% this year.", R.drawable.ic_news_prev),
            NewsItem("New Farming Techniques", "Learn about the latest sustainable farming methods.", R.drawable.ic_news_3),
            NewsItem("Drought Alert", "Regions expected to face severe drought this summer.", R.drawable.ic_news_2)
        )

        val adapter = NewsAdapter(sampleNews)
        binding.newsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.newsRecyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
