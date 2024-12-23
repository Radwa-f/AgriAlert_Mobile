package ma.ensaj.agri_alert.model

data class Crop(
    val name: String,
    val status: String,
    val imageRes: Int? = null // Optional resource ID for the crop image
)
