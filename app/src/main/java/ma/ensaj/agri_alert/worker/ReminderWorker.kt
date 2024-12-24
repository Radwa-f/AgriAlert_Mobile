package ma.ensaj.agri_alert.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import ma.ensaj.agri_alert.util.NotificationHelper

class ReminderWorker(
    val context: Context,
    val params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Reminder"
        val message = inputData.getString("message") ?: ""

        NotificationHelper(context).createNotification(title, message)

        return Result.success()
    }
}