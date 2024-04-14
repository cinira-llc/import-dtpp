package cinira.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

/**
 * [CycleMetafile] holds cycle and approach metadata parsed from a `d-TPP_Metafile.xml` file in a DTPP segment archive.
 */
data class CycleMetafile(

    /**
     * Cycle number.
     */
    val cycle: Int,

    /**
     * Effectivity start date/time.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTF")
    val effectiveStartDateTime: Instant,

    /**
     * Effectivity end date/time.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTF")
    val effectiveEndDateTime: Instant,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val airportDiagrams: List<ChartMetadata> = emptyList(),

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val alternateMinimums: List<ChartMetadata> = emptyList(),

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val approaches: List<ApproachMetadata> = emptyList(),

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val approachCharts: List<ChartMetadata> = emptyList(),

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val departureProcedures: List<ChartMetadata> = emptyList(),

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val diverseVectorAreas: List<ChartMetadata> = emptyList(),

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val hotSpots: List<ChartMetadata> = emptyList(),

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val information: List<ChartMetadata> = emptyList(),

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val landAndHoldShorts: List<ChartMetadata> = emptyList(),

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val obstacleDepartureProcedures: List<ChartMetadata> = emptyList(),

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val radarMinimums: List<ChartMetadata> = emptyList(),

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val standardTerminalArrivals: List<ChartMetadata> = emptyList(),

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val takeoffMinimums: List<ChartMetadata> = emptyList()
)
