package ma.ensaj.agri_alert.model

data class UserProfile(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val crops: List<String>
)
