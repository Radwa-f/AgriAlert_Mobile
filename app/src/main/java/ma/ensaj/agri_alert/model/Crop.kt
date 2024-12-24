package ma.ensaj.agri_alert.model

data class Crop(
    val name: String,
    val status: String, // Backend can provide this if available
    val imageRes: Int? = null // Default to null if the backend provides an image URL
)

