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

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val charts: List<ChartDetails> = emptyList(),

    /**
     * Chart names and details derived from chart PDFs.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val items: List<ItemDetails> = emptyList()
) {
    fun addChart(chart: ChartDetails, item: ItemDetails) =
        copy(
            charts = charts + chart,
            items = items + item
        )

    fun addItem(item: ItemDetails) =
        copy(
            items = items + item
        )
}
