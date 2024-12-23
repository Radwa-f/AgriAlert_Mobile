package ma.ensaj.agri_alert.view.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ma.ensaj.agri_alert.CropsDetailsActivity
import ma.ensaj.agri_alert.R
import ma.ensaj.agri_alert.model.CropAnalysis
import ma.ensaj.agri_alert.network.RetrofitUnsplash
import ma.ensaj.agri_alert.network.UnsplashPhotoResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CropAnalysisAdapter(
    private val cropAnalyses: List<Pair<String, CropAnalysis>>
) : RecyclerView.Adapter<CropAnalysisAdapter.CropAnalysisViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CropAnalysisViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_crop_analysis, parent, false)
        return CropAnalysisViewHolder(view)
    }

    override fun onBindViewHolder(holder: CropAnalysisViewHolder, position: Int) {
        var (cropName, analysis) = cropAnalyses[position]
        holder.bind(cropName, analysis)

        if (cropName.equals("Coffee")) {
            cropName = "$cropName beans"
        } else if (cropName.equals("Rice")) {
            cropName = "$cropName grains"
        }

        // Fetch image for the crop name
        RetrofitUnsplash.api.getRandomPhoto(
            clientId = "SS9TfnNCVbvw9318k1VjoWy7je20hdrVy3_2BlgFmFE",
            query = cropName
        ).enqueue(object : Callback<UnsplashPhotoResponse> {
            override fun onResponse(
                call: Call<UnsplashPhotoResponse>,
                response: Response<UnsplashPhotoResponse>
            ) {
                if (response.isSuccessful) {
                    val imageUrl = response.body()?.urls?.regular
                    Glide.with(holder.itemView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.coffee) // Placeholder image
                        .into(holder.cropImageView)

                    // Set click listener
                    holder.itemView.setOnClickListener {
                        val intent = Intent(holder.itemView.context, CropsDetailsActivity::class.java)
                        intent.putExtra("CROP_NAME", cropName)
                        intent.putExtra("SEVERITY", analysis.overallSeverity)
                        intent.putExtra("ALERTS", analysis.alerts.joinToString("\n") { "${it.title}: ${it.message}" })
                        intent.putExtra("RECOMMENDATIONS", analysis.recommendations.joinToString("\n") { it.message })
                        intent.putExtra("INSIGHTS", analysis.insights.joinToString("\n"))
                        intent.putExtra("IMAGE_URL", imageUrl)
                        holder.itemView.context.startActivity(intent)
                    }
                }
            }

            override fun onFailure(call: Call<UnsplashPhotoResponse>, t: Throwable) {
                // Handle API failure (optional logging or fallback image)
                Glide.with(holder.itemView.context)
                    .load(R.drawable.coffee) // Fallback image
                    .into(holder.cropImageView)
            }
        })
    }

    override fun getItemCount(): Int = cropAnalyses.size

    class CropAnalysisViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cropImageView: ImageView = itemView.findViewById(R.id.news_image)
        private val cropNameTextView: TextView = itemView.findViewById(R.id.tv_crop_name)
        private val severityTextView: TextView = itemView.findViewById(R.id.tv_overall_severity)
        private val alertsTextView: TextView = itemView.findViewById(R.id.tv_alerts_title)

        fun bind(cropName: String, analysis: CropAnalysis) {
            cropNameTextView.text = cropName
            severityTextView.text = "Overall Severity: ${analysis.overallSeverity}"

            // Alerts
            val alerts = analysis.alerts.joinToString("\n") { "${it.title}: ${it.message}" }
            alertsTextView.text = alerts
        }
    }
}
