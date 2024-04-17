package cinira.model

import com.fasterxml.jackson.annotation.JsonInclude

data class MediaEntry(
    val contentType: String,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val keyword: Map<String, Set<String>> = emptyMap(),
    val name: String,
    val size: Long,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val thumbnail: List<ThumbnailEntry> = emptyList(),
    val type: Type
) {
    enum class Type {
        CHART,
        DIFF
    }
}
