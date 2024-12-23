package ma.ensaj.agri_alert

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import ma.ensaj.agri_alert.service.CropAnalysisService
import ma.ensaj.agri_alert.worker.CropAnalysisWorker
import java.util.concurrent.TimeUnit

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        val serviceIntent = Intent(this, CropAnalysisService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        scheduleCropAnalysisWorker()
        scheduleCropAnalysisWorker(this)
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_dark)

        val logo: ImageView = findViewById(R.id.logo)

        // Scale down the logo to 40% of its original size (X and Y scale) over 2000 milliseconds
        logo.animate()
            .scaleX(0.7f)
            .scaleY(0.7f)
            .setDuration(2000)
            .withEndAction {
                        // Translate the logo upward by 100 pixels over 1000 milliseconds
                        logo.animate()
                            .translationY(-100f)
                            .setDuration(1000)
                            .withEndAction {
                                // Make the logo completely transparent (alpha 0) over 4000 milliseconds
                                logo.animate()
                                    .alpha(0f)
                                    .setDuration(4000)
                                    .start()
                            }
                            .start()

            }
            .start()

        val thread = Thread {
            try {
                Thread.sleep(4000)
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        thread.start()
    }

    fun scheduleCropAnalysisWorker(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Requires internet
            .build()

        val cropAnalysisWorkRequest = PeriodicWorkRequestBuilder<CropAnalysisWorker>(2, TimeUnit.SECONDS) // Runs every 12 hours
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "CropAnalysisWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            cropAnalysisWorkRequest
        )
    }

    private fun scheduleCropAnalysisWorker() {
        val workRequest = PeriodicWorkRequestBuilder<CropAnalysisWorker>(
            6, TimeUnit.HOURS
        ).build()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Requires internet
            .build()

        val cropAnalysisWorkRequest = PeriodicWorkRequestBuilder<CropAnalysisWorker>(2, TimeUnit.SECONDS) // Runs every 12 hours
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "CropAnalysisWorker",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    override fun onPause() {
        super.onPause()
        finish()
    }

}