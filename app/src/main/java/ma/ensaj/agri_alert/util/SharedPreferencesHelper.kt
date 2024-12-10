package ma.ensaj.agri_alert.util

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

object SharedPreferencesHelper {
    private const val PREFS_NAME = "MyAppPreferences"

    fun saveLocation(context: Context, latitude: Double, longitude: Double) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putFloat("USER_LATITUDE", latitude.toFloat())
            putFloat("USER_LONGITUDE", longitude.toFloat())
            apply()
        }
    }

    fun getLocation(context: Context): Pair<Double, Double>? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
        val latitude = sharedPreferences.getFloat("USER_LATITUDE", Float.MIN_VALUE)
        val longitude = sharedPreferences.getFloat("USER_LONGITUDE", Float.MIN_VALUE)
        return if (latitude != Float.MIN_VALUE && longitude != Float.MIN_VALUE) {
            Pair(latitude.toDouble(), longitude.toDouble())
        } else {
            null
        }
    }
}