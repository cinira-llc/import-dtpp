package cinira.model

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * [ApproachMetadata] holds information on a single approach which was extracted from a DTPP cycle metafile.
 */
data class ApproachMetadata(

    /**
     * Name of the associated chart file, such as `00106IL31L.PDF`.
     */
    val chartName: String,

    /**
     * Human readable approach title.
     */
    val title: String,

    /**
     * Approach type.
     */
    val type: Type,

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    val circlingOnly: Boolean = false,

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    val converging: Boolean = false,

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    val helicopterOnly: Boolean = false,

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    val highAltitude: Boolean = false,

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    val prm: Boolean = false,

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    val specialAuthorization: Boolean = false,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val categories: List<String> = emptyList(),

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val differentiator: Int? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val heading: Int? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val landmark: String? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val runway: String? = null,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val variant: Char? = null
) {

    /**
     * [Type] defines constants which correspond to general instrument approach types.
     */
    enum class Type {
        GLS,
        GPS,
        ILS,
        ILS_DME,
        LDA,
        LDA_DME,
        LDA_NDB,
        LOC,
        LOC_BC,
        LOC_DME,
        LOC_DME_BC,
        LOC_NDB,
        NDB,
        RNAV_GPS,
        RNAV_RNP,
        SDF,
        TACAN,
        VISUAL,
        VOR,
        VOR_DME
    }
}
