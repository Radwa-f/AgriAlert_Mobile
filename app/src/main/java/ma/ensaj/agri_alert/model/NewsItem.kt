package ma.ensaj.agri_alert.model

data class NewsResponse(
    val news: List<NewsItem>
)

data class NewsItem(
    val title: String?,
    val text: String?,
    val image: String?,
    val url: String?
)

