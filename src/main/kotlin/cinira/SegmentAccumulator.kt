package cinira

import cinira.model.ChartDetails
import cinira.model.CycleMetafile

internal data class SegmentAccumulator(

    /**
     * Chart details, produced for all *except* the final segment, currently `DDTPPA`..`DDTPPD`.
     */
    val charts: List<ChartDetails> = emptyList(),

    /**
     * Cycle metafile, produced only for the final segment, currently `DDTPPE`.
     */
    val metafile: CycleMetafile? = null
)
