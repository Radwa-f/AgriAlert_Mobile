package ma.ensaj.agri_alert.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ma.ensaj.agri_alert.R
import ma.ensaj.agri_alert.model.Alert

class AlertsAdapter(private val items: List<Alert>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_TASK else VIEW_TYPE_ALERT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AlertViewHolder) {
            val alert = items[position]
            holder.bind(alert)
        }
    }

    override fun getItemCount(): Int = items.size

    class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(alert: Alert) {
            // Set title, message, and severity
            itemView.findViewById<TextView>(R.id.tv_alert_title).text = alert.title
            itemView.findViewById<TextView>(R.id.tv_alert_reason).text = alert.message
            itemView.findViewById<TextView>(R.id.tv_alert_severity).apply {
                text = alert.severity.uppercase()

                // Update text color based on severity
                val colorRes = when (alert.severity.uppercase()) {
                    "HIGH" -> R.color.severity_high_text
                    "MEDIUM" -> R.color.severity_medium_text
                    "LOW" -> R.color.severity_low_text
                    else -> R.color.severity_low_text
                }
                setTextColor(context.getColor(colorRes))
            }

            // Set the alert icon (if needed, adjust based on severity)
            val iconView = itemView.findViewById<ImageView>(R.id.iv_alert_icon)
            iconView.setImageResource(R.drawable.ic_alerts)
        }
    }

    companion object {
        private const val VIEW_TYPE_TASK = 0
        private const val VIEW_TYPE_ALERT = 1
    }
}
