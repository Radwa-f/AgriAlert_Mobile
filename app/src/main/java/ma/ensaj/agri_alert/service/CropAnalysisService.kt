package ma.ensaj.agri_alert.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ma.ensaj.agri_alert.CropsDetailsActivity
import ma.ensaj.agri_alert.R
import ma.ensaj.agri_alert.model.WeatherAnalysisRequest
import ma.ensaj.agri_alert.network.RetrofitClient
import ma.ensaj.agri_alert.network.WeatherApi
import ma.ensaj.agri_alert.util.SharedPreferencesHelper

class CropAnalysisService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            fetchCropAnalysis()
        }
        return START_STICKY
    }

    private suspend fun fetchCropAnalysis() {
        val token = SharedPreferencesHelper.getToken(this) ?: return
        val location = SharedPreferencesHelper.getLocation(this) ?: return

        try {
            // Fetch weather data
            val weatherResponse = WeatherApi.retrofitService.getWeather(
                latitude = location.first,
                longitude = location.second,
                daily = "temperature_2m_max,temperature_2m_min,precipitation_sum",
                hourly = "precipitation",
                timezone = "auto"
            )

            val maxTemp = weatherResponse.daily.temperatureMax[1]
            val minTemp = weatherResponse.daily.temperatureMin[1]
            val maxRain = weatherResponse.daily.precipitationSum.maxOrNull() ?: 0.0
            val minRain = weatherResponse.daily.precipitationSum.minOrNull() ?: 0.0

            // Fetch crop analysis
            val userProfileResponse = RetrofitClient.instance.getUserProfile("Bearer $token")
            if (userProfileResponse.isSuccessful) {
                val userCrops = userProfileResponse.body()?.crops ?: return

                val request = WeatherAnalysisRequest(
                    maxTemp = maxTemp,
                    minTemp = minTemp,
                    maxRain = maxRain,
                    minRain = minRain,
                    cropNames = userCrops
                )

                val analysisResponse = RetrofitClient.instance.getWeatherAnalysis(request)
                if (analysisResponse.isSuccessful) {
                    val cropAnalysisData = analysisResponse.body()
                    SharedPreferencesHelper.saveCropAnalysis(this, cropAnalysisData)

                    // Trigger notification
                    triggerNotification(
                        title = "New Crop Analysis Available",
                        message = "Tap to view recommendations for your crops."
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun triggerNotification(title: String, message: String) {
        val channelId = "CropAnalysisChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Crop Analysis Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val intent = Intent(this, CropsDetailsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_alerts)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        manager.notify(102, notification)
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        val channelId = "ForegroundServiceChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Foreground Service Notifications",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_alerts)
            .setContentTitle("AgriAlert Service")
            .setContentText("Monitoring crop conditions in the background.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
