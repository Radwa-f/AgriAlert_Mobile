package ma.ensaj.agri_alert.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ma.ensaj.agri_alert.R
import ma.ensaj.agri_alert.model.Crop

class CropsAdapter(private val crops: List<Crop>) : RecyclerView.Adapter<CropsAdapter.CropViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CropViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_crop, parent, false)
        return CropViewHolder(view)
    }

    override fun onBindViewHolder(holder: CropViewHolder, position: Int) {
        val crop = crops[position]
        holder.tvCropName.text = crop.name
        holder.tvCropStatus.text = crop.status

        // Set image if available; otherwise, use a placeholder
        holder.ivCropImage.setImageResource(crop.imageRes ?: R.drawable.ic_crops)
    }

    override fun getItemCount(): Int = crops.size

    class CropViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCropImage: ImageView = itemView.findViewById(R.id.iv_crop_image)
        val tvCropName: TextView = itemView.findViewById(R.id.tv_crop_name)
        val tvCropStatus: TextView = itemView.findViewById(R.id.tv_crop_status)
    }
}
