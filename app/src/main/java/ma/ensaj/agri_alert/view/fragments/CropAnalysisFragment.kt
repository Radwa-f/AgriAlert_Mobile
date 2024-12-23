package ma.ensaj.agri_alert.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ma.ensaj.agri_alert.R
import ma.ensaj.agri_alert.model.CropAnalysis
import ma.ensaj.agri_alert.util.SharedPreferencesHelper
import ma.ensaj.agri_alert.view.adapters.CropAnalysisAdapter

class CropAnalysisFragment : Fragment() {

    private lateinit var cropAnalysisRecyclerView: RecyclerView
    private lateinit var cropAnalysisAdapter: CropAnalysisAdapter
    private val cropAnalyses = mutableListOf<Pair<String, CropAnalysis>>() // List of crop name and analysis

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crop_analysis, container, false)
        cropAnalysisRecyclerView = view.findViewById(R.id.rv_crop_analysis)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        cropAnalysisAdapter = CropAnalysisAdapter(cropAnalyses)
        cropAnalysisRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        cropAnalysisRecyclerView.adapter = cropAnalysisAdapter

        // Fetch data and populate UI
        fetchCropAnalysisData()
    }

    private fun fetchCropAnalysisData() {
        // Retrieve the crop analysis response from SharedPreferences
        val cropAnalysisResponse = SharedPreferencesHelper.getCropAnalysis(requireContext())

        if (cropAnalysisResponse?.cropAnalyses.isNullOrEmpty()) {
            Log.e("CropAnalysisFragment", "No crop analysis data found.")
            return
        }

        if (cropAnalysisResponse != null) {
            Log.d("CropAnalysisFragment", "Crop Analysis Data: ${cropAnalysisResponse.cropAnalyses}")
        }

        // Populate RecyclerView with crop analysis data
        cropAnalyses.clear()
        if (cropAnalysisResponse != null) {
            cropAnalyses.addAll(cropAnalysisResponse.cropAnalyses.toList())
            cropAnalysisAdapter.notifyDataSetChanged()
        }
        cropAnalysisAdapter.notifyDataSetChanged()
    }


    companion object {
        fun newInstance(cropAnalysis: Map<String, CropAnalysis>): CropAnalysisFragment {
            val fragment = CropAnalysisFragment()
            val args = Bundle()
            args.putSerializable("cropAnalysis", HashMap(cropAnalysis))
            fragment.arguments = args
            return fragment
        }
    }
}
