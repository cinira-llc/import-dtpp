package cinira

import cinira.model.ApproachMetadata
import cinira.model.ChartMetadata
import cinira.model.SegmentIndex
import cinira.parser.ApproachChartTitleLexer
import cinira.parser.ApproachChartTitleParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.InputStream
import java.lang.Integer.parseInt
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.Attribute

internal class MetafileParser {

    fun parse(index: SegmentIndex, content: InputStream) =
        inputFactory.createXMLEventReader(content).let { reader ->
            val context = Stack<Pair<String, MutableMap<String, String?>>>()
            var contents = MetafileAccumulator()
            while (reader.hasNext()) {
                val event = reader.nextEvent()
                when {
                    event.isStartElement -> {

                        /* Started an element; push a new entry onto the context stack, including all attributes
                        (and, if it has ancestor(s), ancestor attributes.) Note that ancestor attributes are
                        prefixed by the local name of the ancestor element, followed by a dot. This allows us to
                        handle things like <airport_name/>, <city_name/>, and <state_code/>, which each have an "ID"
                        attribute which holds useful data. We end up with "airport_name.ID", "city_name.ID", and
                        "state_code.ID" in the attribute map. */
                        val element = event.asStartElement()
                        val name = element.name.localPart
                        val attributes = element.attributes.asSequence()
                            .associateBy({ attr -> "$name.${attr.name.localPart}" }, Attribute::getValue)
                        if (!context.isEmpty()) {
                            val parentAttributes = context.peek().second
                            context.push(name to (parentAttributes + attributes.toMutableMap()).toMutableMap())
                        } else {
                            val prefix = index.segment.dropLast(1)
                            val suffixes = 'A'..index.segment.last()
                            val segments = suffixes.map { suffix -> prefix + suffix }.sorted().toSet()
                            contents = contents.attributes(
                                cycle = attributes["digital_tpp.cycle"]!!.toInt(),
                                segments = segments,
                                effectiveEndDateTime = parseEffectiveDate(attributes["digital_tpp.to_edate"]!!),
                                effectiveStartDateTime = parseEffectiveDate(attributes["digital_tpp.from_edate"]!!)
                            )
                            context.push(name to attributes.toMutableMap())
                        }
                    }

                    event.isEndElement -> {
                        val (name, attributes) = context.pop()
                        if (context.isNotEmpty()) {
                            if ("record" == name) {
                                contents = updateContents(contents, attributes)
                            } else {

                                /* Finished some other element; if its parent is a <record/> element, assign @text
                                pseudo-attribute, if non-empty, to the parent under this element's local name. */
                                val (parentName, parentAttributes) = context.peek()
                                val value = attributes["@text"]?.trim() ?: ""
                                if ("" != value && "record" == parentName) {
                                    parentAttributes[name] = value
                                }
                            }
                        }
                    }

                    event.isCharacters -> {

                        /* Received text characters; add to a special "@text" attribute of the parent element. */
                        val characters = event.asCharacters()
                        if (!characters.isWhiteSpace) {
                            val (_, attributes) = context.peek()
                            if (!attributes.containsKey("@text")) {
                                attributes["@text"] = characters.data
                            } else {
                                attributes["@text"] += characters.data
                            }
                        }
                    }
                }
            }
            contents.completeMetafile()
        }

    /**
     * Assemble parsed element attributes and text nodes into a [ChartMetadata] object.
     *
     * @param attributes the attributes of the `<record/>` element and its parents, and any `@text` sub-elements of the
     * `<record/>` element.
     * @return [ChartMetadata]
     */
    private fun updateContents(contents: MetafileAccumulator, attributes: Map<String, String?>) =
        attributes["chartseq"]!!.toInt().let { seq ->
            val chart = ChartMetadata(
                name = attributes["pdf_name"]!!,
                title = attributes["chart_name"]!!,
                type = ChartMetadata.Type.fromCodeAndSeq(attributes["chart_code"]!!, seq),
                seq = seq,
                airportIcaoIdent = attributes["airport_name.icao_ident"]!!,
                airportIdent = attributes["airport_name.apt_ident"]!!,
                airportName = attributes["airport_name.ID"]!!,
                cityName = attributes["city_name.ID"]!!,
                procedureUid = attributes["procuid"]?.let(::parseInt),
                stateCode = attributes["state_code.ID"]!!,
                amendment = attributes["amdtnum"],
                amendmentDate = attributes["amdtdate"]?.let { date ->
                    LocalDate.parse(date, amendmentDateFormat)
                }
            )
            when (chart.type) {
                ChartMetadata.Type.AIRPORT_DIAGRAM -> contents.airportDiagram(chart)
                ChartMetadata.Type.ALTERNATE_MINIMUMS -> contents.alternateMinimums(chart)
                ChartMetadata.Type.DEPARTURE_PROCEDURE -> contents.departureProcedure(chart)
                ChartMetadata.Type.DEPARTURE_PROCEDURE_ATTENTION_ALL_USERS_PAGE -> contents.information(chart)
                ChartMetadata.Type.DIVERSE_VECTOR_AREA -> contents.diverseVectorArea(chart)
                ChartMetadata.Type.HOT_SPOT -> contents.hotSpot(chart)
                ChartMetadata.Type.INSTRUMENT_APPROACH_PROCEDURE -> if ("PRM AAUP" == chart.title) {
                    contents.information(chart)
                } else {
                    approachFromTitle(chart.name, chart.title)
                        .fold(contents.approachChart(chart)) { acc, approach ->
                            acc.approachProcedure(
                                ApproachMetadata(
                                    chartName = chart.name,
                                    title = approach.title,
                                    type = approach.type,
                                    circlingOnly = approach.circlingOnly,
                                    converging = approach.converging,
                                    helicopterOnly = approach.helicopterOnly,
                                    highAltitude = approach.highAltitude,
                                    prm = approach.prm,
                                    specialAuthorization = approach.specialAuthorization,
                                    categories = approach.categories,
                                    differentiator = approach.differentiator,
                                    heading = approach.heading,
                                    landmark = approach.landmark,
                                    runway = approach.runway?.trimStart('0'),
                                    variant = approach.variant
                                )
                            )
                        }
                }

                ChartMetadata.Type.LAND_AND_HOLD_SHORT -> contents.landAndHoldShort(chart)
                ChartMetadata.Type.OBSTACLE_DEPARTURE_PROCEDURE -> contents.obstacleDepartureProcedure(chart)
                ChartMetadata.Type.RADAR_MINIMUMS -> contents.radarMinimums(chart)
                ChartMetadata.Type.STANDARD_TERMINAL_ARRIVAL -> contents.standardTerminalArrival(chart)
                ChartMetadata.Type.TAKEOFF_MINIMUMS -> contents.takeoffMinimums(chart)
            }
        }

    /**
     * Parse approach data from a chart title.
     *
     * @param name the chart file name.
     * @param title the chart title.
     * @return [List] of [ApproachMetadata]
     */
    private fun approachFromTitle(name: String, title: String) =
        ApproachChartTitleLexer(CharStreams.fromString(title)).let { lexer ->
            val parser = ApproachChartTitleParser(CommonTokenStream(lexer))
            ExtractIapData(name, title).also { listener ->
                parser.addParseListener(listener)
                parser.name()
            }.approaches.toList()
        }


    private fun parseEffectiveDate(value: String) =
        effectiveDateTimeFormat.parse(value, Instant::from)

    companion object {
        private val amendmentDateFormat = DateTimeFormatter.ofPattern("M/d/yyyy")
        private val effectiveDateTimeFormat = DateTimeFormatterBuilder().appendPattern("HHmm'Z'  MM/dd/")
            .appendValueReduced(ChronoField.YEAR, 2, 2, 2020)
            .toFormatter()
            .withZone(ZoneId.of("UTC"))
        private val inputFactory = XMLInputFactory.newDefaultFactory()
    }
}