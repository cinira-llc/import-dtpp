package cinira

import cinira.model.ChartDetails
import cinira.model.ItemDetails

data class ArtifactAccumulator(
    val chartDetails: List<ChartDetails> = emptyList(),
    val items: List<ItemDetails> = emptyList()
) {
    fun addChart(details: ChartDetails, item: ItemDetails) =
        copy(
            chartDetails = chartDetails + details,
            items = items + item
        )

    fun addItem(item: ItemDetails) =
        copy(
            items = items + item
        )
}
