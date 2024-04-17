package cinira.model

import com.fasterxml.jackson.annotation.JsonInclude

data class DatasetEntry(

    /**
     * Natural (inside any wrapper) content type of the item.
     */
    val contentType: String,

    val name: String,

    /**
     * Natural (inside any wrapper) size of the item.
     */
    val size: Long,

    /**
     * Item type.
     */
    val type: Type,

    /**
     * Wrapper content type, if any, such as `application/bzip2`.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val wrapperContentType: String? = null,
) {
    enum class Type {
        METAFILE
    }
}
