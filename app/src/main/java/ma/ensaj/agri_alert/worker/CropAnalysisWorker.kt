package ma.ensaj.agri_alert.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ma.ensaj.agri_alert.CropsDetailsActivity
import ma.ensaj.agri_alert.R
import ma.ensaj.agri_alert.model.WeatherAnalysisRequest
import ma.ensaj.agri_alert.network.RetrofitClient
import ma.ensaj.agri_alert.network.WeatherApi
import ma.ensaj.agri_alert.util.SharedPreferencesHelper

class CropAnalysisWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Fetch user token and location
        val token = SharedPreferencesHelper.getToken(applicationContext) ?: return Result.failure()
        val location = SharedPreferencesHelper.getLocation(applicationContext) ?: return Result.failure()

        val latitude = location.first
        val longitude = location.second

        return try {
            // Fetch weather data
            val weatherResponse = withContext(Dispatchers.IO) {
                WeatherApi.retrofitService.getWeather(
                    latitude = latitude,
                    longitude = longitude,
                    daily = "temperature_2m_max,temperature_2m_min,precipitation_sum",
                    hourly = "precipitation",
                    timezone = "auto"
                )
            }

            val maxTemp = weatherResponse.daily.temperatureMax[1]
            val minTemp = weatherResponse.daily.temperatureMin[1]
            val maxRain = weatherResponse.daily.precipitationSum.maxOrNull() ?: 0.0
            val minRain = weatherResponse.daily.precipitationSum.minOrNull() ?: 0.0

            // Fetch crop analysis
            val userProfileResponse = withContext(Dispatchers.IO) {
                RetrofitClient.instance.getUserProfile("Bearer $token")
            }

            if (userProfileResponse.isSuccessful) {
                val userCrops = userProfileResponse.body()?.crops ?: return Result.failure()

                val request = WeatherAnalysisRequest(
                    maxTemp = maxTemp,
                    minTemp = minTemp,
                    maxRain = maxRain,
                    minRain = minRain,
                    cropNames = userCrops
                )

                val analysisResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.getWeatherAnalysis(request)
                }

                if (analysisResponse.isSuccessful) {
                    val cropAnalysisData = analysisResponse.body()
                    SharedPreferencesHelper.saveCropAnalysis(applicationContext, cropAnalysisData)

                    // Trigger a notification
                    triggerNotification(
                        title = "New Crop Analysis Available",
                        message = "Tap to view recommendations for your crops."
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun triggerNotification(title: String, message: String) {
        val channelId = "AgriAlert_Channel"
        val notificationId = 101

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "AgriAlert Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for AgriAlert recommendations"
            }
            val notificationManager =
                applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, CropsDetailsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_alerts)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted, log or handle this case
            return
        }
        NotificationManagerCompat.from(applicationContext).notify(notificationId, notification)
    }
}
