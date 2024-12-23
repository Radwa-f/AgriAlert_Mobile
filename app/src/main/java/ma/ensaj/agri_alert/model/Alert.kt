package ma.ensaj.agri_alert.model

data class Alert(
    val type: String,
    val title: String,
    val message: String,
    val severity: String
)
