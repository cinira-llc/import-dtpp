package cinira.model

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * [SegmentIndex] defines the format of the `index-[segment].json` file which is generated when each DTPP segment
 * archive is imported into blob storage.
 */
data class SegmentIndex(

    /**
     * Cycle number.
     */
    val cycle: Int,

    /**
     * Segment name, `DDTPPA` through (at the time of this writing) `DDTPPE`.
     */
    val segment: String,

    /**
     * Data files extracted from the segment.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val dataset: List<DatasetEntry> = emptyList(),

    /**
     * Media files extracted from the segment.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val media: List<MediaEntry> = emptyList()
) {
    fun addMedia(media: MediaEntry) =
        copy(
            media = this.media + media
        )

    fun addDataset(dataset: DatasetEntry) =
        copy(
            dataset = this.dataset + dataset
        )
}
