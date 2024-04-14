package cinira.model

import com.fasterxml.jackson.annotation.JsonInclude
import java.net.URI

data class ItemDetails(

    /**
     * Path to the item in blob storage.
     */
    val uri: URI,

    /**
     * Natural (inside any wrapper) content type of the item.
     */
    val contentType: String,

    /**
     * Natural (inside any wrapper) size of the item.
     */
    val size: Long,

    /**
     * Item type.
     */
    val type: ItemType,

    /**
     * Wrapper content type, if any, such as `application/bzip2`.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val wrapperContentType: String? = null,
) {
    enum class ItemType {
        CHART,
        DIFF,
        METAFILE
    }
}
