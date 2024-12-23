package ma.ensaj.agri_alert.util

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import ma.ensaj.agri_alert.model.CropAnalysisResponse

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

    fun saveToken(context: Context, token: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("AUTH_TOKEN", token)
            apply()
        }
    }

    fun getToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
        return sharedPreferences.getString("AUTH_TOKEN", null)
    }
    fun saveCropAnalysis(context: Context, cropAnalysis: CropAnalysisResponse?) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
        val analysisJson = Gson().toJson(cropAnalysis)
        with(sharedPreferences.edit()) {
            putString("CROP_ANALYSIS", analysisJson)
            apply()
        }
    }

    fun getCropAnalysis(context: Context): CropAnalysisResponse? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, AppCompatActivity.MODE_PRIVATE)
        val analysisJson = sharedPreferences.getString("CROP_ANALYSIS", null)
        return if (analysisJson != null) {
            Gson().fromJson(analysisJson, CropAnalysisResponse::class.java)
        } else {
            null
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