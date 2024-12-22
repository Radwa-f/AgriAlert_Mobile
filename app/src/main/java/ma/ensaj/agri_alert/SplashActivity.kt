package ma.ensaj.agri_alert

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

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

    override fun onPause() {
        super.onPause()
        finish()
    }

}