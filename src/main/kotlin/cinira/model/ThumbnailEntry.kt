package cinira.model

data class ThumbnailEntry(
    val contentType: String,
    val dimensions: Array<Int>,
    val dpi: Int,
    val size: Long
)
