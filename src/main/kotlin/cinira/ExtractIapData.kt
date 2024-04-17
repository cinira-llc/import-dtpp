package cinira

import cinira.model.ApproachMetadata
import cinira.parser.ApproachChartTitleBaseListener
import cinira.parser.ApproachChartTitleParser
import org.antlr.v4.runtime.Token
import java.lang.IllegalArgumentException

/**
 * [ExtractIapData] is an implementation of the [ApproachChartTitleBaseListener] interface which parses approach
 * procedure metadata from an approach chart title.
 */
internal class ExtractIapData(
    private val chartName: String,
    private val title: String
) : ApproachChartTitleBaseListener() {

    /**
     * Intermediate data collected for the approach(es) depicted by the chart.
     */
    var approaches = mutableListOf<ApproachMetadata>()

    override fun enterVisual(ctx: ApproachChartTitleParser.VisualContext) {
        approaches.add(
            ApproachMetadata(
                chartName = chartName,
                title = title,
                type = ApproachMetadata.Type.VISUAL
            )
        )
    }

    override fun exitApproach(ctx: ApproachChartTitleParser.ApproachContext) {
        if (null != ctx.variant) {
            approaches.replaceLast { app ->
                app.copy(
                    variant = ctx.variant.text[0]
                )
            }
        }
    }

    override fun exitCopter(ctx: ApproachChartTitleParser.CopterContext) {
        approaches.replaceAll { app ->
            app.copy(
                helicopterOnly = true
            )
        }
        if (null != ctx.heading) {
            approaches.replaceAll { app ->
                app.copy(
                    heading = ctx.heading.text.toInt()
                )
            }
        }
    }

    override fun exitGuidance(ctx: ApproachChartTitleParser.GuidanceContext) {
        approaches.add(
            ApproachMetadata(
                chartName = chartName,
                title = title,
                type = ctx.toApproachType(),
                converging = null != ctx.converging,
                differentiator = ctx.differentiator?.text?.toInt(),
                prm = null != ctx.prm
            )
        )
    }

    override fun exitInstrument(ctx: ApproachChartTitleParser.InstrumentContext) {
        approaches.replaceLast { app ->
            app.copy(
                highAltitude = null != ctx.hi
            )
        }
        if (null != ctx.circle) {
            approaches.replaceAll { app ->
                if (null != app.variant) {
                    throw IllegalStateException()
                }
                app.copy(
                    circlingOnly = true,
                    variant = ctx.circle.text[0]
                )
            }
        }
    }

    override fun exitLandmark(ctx: ApproachChartTitleParser.LandmarkContext) {
        approaches.replaceAll { app ->
            app.copy(
                landmark = ctx.children.joinToString(" ")
            )
        }
    }

    override fun exitRunways(ctx: ApproachChartTitleParser.RunwaysContext) {
        val number = ctx.number.text
        if (ctx.positions.isEmpty()) {
            approaches.replaceAll { app ->
                app.copy(
                    runway = number
                )
            }
        } else if (1 == ctx.positions.size) {
            approaches.replaceAll { app ->
                app.copy(
                    runway = "$number${ctx.positions.first().text}"
                )
            }
        } else {
            val original = approaches.toCollection(mutableListOf())
            approaches.clear()
            approaches.addAll(ctx.positions.flatMap { position ->
                val runway = "$number${position.text}"
                original.map { app ->
                    app.copy(
                        runway = runway
                    )
                }
            })
        }
    }

    override fun exitSuffix(ctx: ApproachChartTitleParser.SuffixContext) {
        approaches.replaceAll { app ->
            app.copy(
                converging = null != ctx.converging,
                specialAuthorization = null != ctx.sa,
                categories = ctx.categories.map(Token::getText)
            )
        }
    }

    override fun exitName(ctx: ApproachChartTitleParser.NameContext) {
        approaches.replaceAll { app ->
            app.copy(
                title = formatTitle(app)
            )
        }
    }

    private fun formatTitle(approach: ApproachMetadata) =
        StringBuilder().apply {
            if (approach.converging) {
                append("CONVERGING ")
            }
            if (approach.helicopterOnly) {
                append("COPTER ")
            } else if (approach.highAltitude) {
                append("HI-")
            }
            append(
                when (approach.type) {
                    ApproachMetadata.Type.GPS -> "GPS"
                    ApproachMetadata.Type.GLS -> "GLS"
                    ApproachMetadata.Type.ILS -> "ILS"
                    ApproachMetadata.Type.ILS_DME -> "ILS/DME"
                    ApproachMetadata.Type.LDA -> "LDA"
                    ApproachMetadata.Type.LDA_DME -> "LDA/DME"
                    ApproachMetadata.Type.LDA_NDB -> "LDA/NDB"
                    ApproachMetadata.Type.LOC -> "LOC"
                    ApproachMetadata.Type.LOC_BC -> "LOC BC"
                    ApproachMetadata.Type.LOC_DME -> "LOC/DME"
                    ApproachMetadata.Type.LOC_DME_BC -> "LOC/DME BC"
                    ApproachMetadata.Type.LOC_NDB -> "LOC/NDB"
                    ApproachMetadata.Type.NDB -> "NDB"
                    ApproachMetadata.Type.RNAV_GPS -> "RNAV (GPS)"
                    ApproachMetadata.Type.RNAV_RNP -> "RNAV (RNP)"
                    ApproachMetadata.Type.SDF -> "SDF"
                    ApproachMetadata.Type.TACAN -> "TACAN"
                    ApproachMetadata.Type.VISUAL -> "${approach.landmark} VISUAL"
                    ApproachMetadata.Type.VOR -> "VOR"
                    ApproachMetadata.Type.VOR_DME -> "VOR/DME"
                }
            )
            approach.differentiator?.let { differentiator ->
                append('-').append(differentiator)
            }
            approach.variant?.let { variant ->
                if (approach.circlingOnly) {
                    append('-').append(variant)
                } else {
                    append(' ').append(variant)
                }
            }
            if (approach.prm) {
                append(" PRM")
            }
            if (!approach.circlingOnly) {
                append(approach.heading?.let { heading ->
                    " ${heading.toString().padStart(3, '0')}"
                } ?: " RWY ${approach.runway!!}")
            }
        }.toString()
}

/**
 * Get the appropriate [ApproachType] per the content of a `guidance` parser token.
 *
 * @return [ApproachType]
 * @throws IllegalArgumentException if the state of the parser token does not correspond to a supported value.
 */
private fun ApproachChartTitleParser.GuidanceContext.toApproachType() =
    when (type.type) {
        ApproachChartTitleParser.GLS -> ApproachMetadata.Type.GLS
        ApproachChartTitleParser.GPS -> ApproachMetadata.Type.GPS
        ApproachChartTitleParser.ILS ->
            when (equipment?.type) {
                null -> ApproachMetadata.Type.ILS
                ApproachChartTitleParser.DME -> ApproachMetadata.Type.ILS_DME
                else -> throw IllegalArgumentException("Unsupported ILS equipment [${equipment.text}].")
            }
        ApproachChartTitleParser.LDA ->
            when (equipment?.type) {
                null -> ApproachMetadata.Type.LDA
                ApproachChartTitleParser.DME -> ApproachMetadata.Type.LDA_DME
                ApproachChartTitleParser.NDB -> ApproachMetadata.Type.LDA_NDB
                else -> throw IllegalArgumentException("Unsupported LDA equipment [${equipment.text}].")
            }
        ApproachChartTitleParser.LOC -> when (equipment?.type) {
            null ->
                if (null != bc) {
                    ApproachMetadata.Type.LOC_BC
                } else {
                    ApproachMetadata.Type.LOC
                }
            ApproachChartTitleParser.DME ->
                if (null != bc) {
                    ApproachMetadata.Type.LOC_DME_BC
                } else {
                    ApproachMetadata.Type.LOC_DME
                }
            ApproachChartTitleParser.NDB -> ApproachMetadata.Type.LOC_NDB
            else -> throw IllegalArgumentException("Unsupported LOC equipment [${equipment.text}].")
        }
        ApproachChartTitleParser.NDB -> ApproachMetadata.Type.NDB
        ApproachChartTitleParser.RNAV ->
            when (equipment?.type) {
                ApproachChartTitleParser.GPS -> ApproachMetadata.Type.RNAV_GPS
                ApproachChartTitleParser.RNP -> ApproachMetadata.Type.RNAV_RNP
                null -> throw IllegalArgumentException("Unidentified RNAV equipment.")
                else -> throw IllegalArgumentException("Unsupported RNAV equipment [${equipment.text}].")
            }
        ApproachChartTitleParser.SDF -> ApproachMetadata.Type.SDF
        ApproachChartTitleParser.TACAN -> ApproachMetadata.Type.TACAN
        ApproachChartTitleParser.VOR ->
            when (equipment?.type) {
                null -> ApproachMetadata.Type.VOR
                ApproachChartTitleParser.DME -> ApproachMetadata.Type.VOR_DME
                else -> throw IllegalArgumentException("Unsupported VOR equipment [${equipment.text}].")
            }
        else -> throw IllegalArgumentException("Unsupported guidance [${type.text}]")
    }

/**
 * Remove the last item in a mutable list, pass it through an `operator` function, and add the result back to the end of
 * the list. This is the equivalent of [MutableList.replaceAll] except that it operates only on the last element of the
 * list.
 *
 * @param operator the operator which will produce the replacement.
 */
fun <T> MutableList<T>.replaceLast(operator: (T) -> T) {
    add(operator(removeLast()))
}
