package ma.ensaj.agri_alert.model

data class RegistrationRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val location: Location,
    val crops: List<String>
)

data class Location(
    val latitude: Double,
    val longitude: Double
)
