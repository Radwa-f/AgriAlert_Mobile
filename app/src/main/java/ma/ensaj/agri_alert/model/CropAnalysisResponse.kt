package ma.ensaj.agri_alert.model

data class CropAnalysisResponse(
    val cropAnalyses: Map<String, CropAnalysis>,
    val errors: List<String>
)

data class CropAnalysis(
    val overallSeverity: String,
    val alerts: List<Alert>,
    val recommendations: List<Recommendation>,
    val insights: List<String>
)

data class Recommendation(
    val message: String
)
