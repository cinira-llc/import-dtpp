package cinira

import cinira.model.ApproachMetadata
import cinira.model.ChartMetadata
import cinira.model.CycleMetafile
import java.time.Instant

/**
 * [MetafileAccumulator]
 */
internal data class MetafileAccumulator(
    val metafile: CycleMetafile? = null
) {
    fun attributes(cycle: Int, segments: Set<String>, effectiveStartDateTime: Instant, effectiveEndDateTime: Instant) =
        if (null != metafile) {
            throw IllegalStateException("Cycle attributes already set.")
        } else {
            copy(
                metafile = CycleMetafile(
                    cycle = cycle,
                    segments = segments,
                    effectiveStartDateTime = effectiveStartDateTime,
                    effectiveEndDateTime = effectiveEndDateTime
                )
            )
        }

    fun airportDiagram(chart: ChartMetadata) = copy(
        metafile = metafile {
            copy(
                airportDiagrams = airportDiagrams + chart
            )
        }
    )

    fun alternateMinimums(chart: ChartMetadata) = copy(
        metafile = metafile {
            copy(
                alternateMinimums = alternateMinimums + chart
            )
        }
    )

    fun approachChart(chart: ChartMetadata) = copy(
        metafile = metafile {
            copy(
                approachCharts = approachCharts + chart
            )
        }
    )

    fun approachProcedure(approach: ApproachMetadata) = copy(
        metafile = metafile {
            copy(
                approaches = approaches + approach
            )
        }
    )

    fun departureProcedure(chart: ChartMetadata) = copy(
        metafile = metafile {
            copy(
                departureProcedures = departureProcedures + chart
            )
        }
    )

    fun diverseVectorArea(chart: ChartMetadata) = copy(
        metafile = metafile {
            copy(
                diverseVectorAreas = diverseVectorAreas + chart
            )
        }
    )

    fun hotSpot(chart: ChartMetadata) = copy(
        metafile = metafile {
            copy(
                hotSpots = hotSpots + chart
            )
        }
    )

    fun information(chart: ChartMetadata) = copy(
        metafile = metafile {
            copy(
                information = information + chart
            )
        }
    )

    fun landAndHoldShort(chart: ChartMetadata) = copy(
        metafile = metafile {
            copy(
                landAndHoldShorts = landAndHoldShorts + chart
            )
        }
    )

    fun obstacleDepartureProcedure(chart: ChartMetadata) = copy(
        metafile = metafile {
            copy(
                obstacleDepartureProcedures = obstacleDepartureProcedures + chart
            )
        }
    )

    fun radarMinimums(chart: ChartMetadata) = copy(
        metafile = metafile {
            copy(
                radarMinimums = radarMinimums + chart
            )
        }
    )

    fun standardTerminalArrival(chart: ChartMetadata) = copy(
        metafile = metafile {
            copy(
                standardTerminalArrivals = standardTerminalArrivals + chart
            )
        }
    )

    fun takeoffMinimums(chart: ChartMetadata) = copy(
        metafile = metafile {
            copy(
                takeoffMinimums = takeoffMinimums + chart
            )
        }
    )

    fun completeMetafile() =
        metafile {
            copy(
                airportDiagrams = airportDiagrams.sortedBy(ChartMetadata::name),
                alternateMinimums = alternateMinimums.sortedBy(ChartMetadata::name),
                approaches = approaches.sortedBy(ApproachMetadata::chartName),
                approachCharts = approachCharts.sortedBy(ChartMetadata::name),
                departureProcedures = departureProcedures.sortedBy(ChartMetadata::name),
                diverseVectorAreas = diverseVectorAreas.sortedBy(ChartMetadata::name),
                hotSpots = hotSpots.sortedBy(ChartMetadata::name),
                information = information.sortedBy(ChartMetadata::name),
                landAndHoldShorts = landAndHoldShorts.sortedBy(ChartMetadata::name),
                obstacleDepartureProcedures = obstacleDepartureProcedures.sortedBy(ChartMetadata::name),
                radarMinimums = radarMinimums.sortedBy(ChartMetadata::name),
                standardTerminalArrivals = standardTerminalArrivals.sortedBy(ChartMetadata::name),
                takeoffMinimums = takeoffMinimums.sortedBy(ChartMetadata::name)
            )
        }

    private fun metafile(closure: CycleMetafile.() -> CycleMetafile) =
        closure(metafile!!)
}
