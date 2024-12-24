package ma.ensaj.agri_alert.model

data class UserProfile(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val location: Location?,
    val crops: List<String>
)

