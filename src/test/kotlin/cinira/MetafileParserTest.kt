package cinira

import cinira.dtpp.ApproachMetadata
import cinira.dtpp.ChartMetadata
import cinira.dtpp.SegmentIndex
import org.apache.commons.io.input.CloseShieldInputStream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate
import java.util.zip.ZipInputStream

/**
 * [MetafileParserTest] provides unit test coverage for [MetafileParser].
 *
 * Requires a known test segment (see [SEGMENT_RESOURCE]) to be available via the `CINIRA_TEST_DATASET_ROOT` environment
 * variable.
 */
@EnabledIfEnvironmentVariable(
    named = "CINIRA_TEST_DATASET_ROOT",
    matches = ".+",
    disabledReason = "Test datasets not found"
)
@ExtendWith(SpringExtension::class)
internal class MetafileParserTest {

    @Test
    fun `parse() parses a known segment correctly`(@Value(SEGMENT_RESOURCE) segment: Resource) {
        assertThat(segment.exists()).isTrue
        val parser = MetafileParser()
        val index = SegmentIndex(2404, "DDTPPE")
        val metafile = segment.inputStream.let(::ZipInputStream).use { zip ->
            generateSequence(zip::getNextEntry)
                .filter { entry -> "d-TPP_Metafile.xml" == entry.name }
                .map { parser.parse(index, CloseShieldInputStream.wrap(zip)) }
                .single()
        }

        /* Cycle attributes. */
        assertThat(metafile.cycle).isEqualTo(2404)
        assertThat(metafile.segments).isEqualTo(setOf("DDTPPA", "DDTPPB", "DDTPPC", "DDTPPD", "DDTPPE"))
        assertThat(metafile.effectiveEnd).isEqualTo("2024-05-16T09:01:00Z")
        assertThat(metafile.effectiveStart).isEqualTo("2024-04-18T09:01:00Z")

        /* Airport diagrams. */
        assertThat(metafile.airportDiagrams)
            .hasSize(851)
            .first()
            .isEqualTo(
                ChartMetadata(
                    name = "00001AD.PDF",
                    title = "AIRPORT DIAGRAM",
                    type = ChartMetadata.Type.AIRPORT_DIAGRAM,
                    seq = 70000,
                    airportIcaoIdent = "KABI",
                    airportIdent = "ABI",
                    airportName = "ABILENE RGNL",
                    cityName = "ABILENE",
                    procedureUid = 1,
                    stateCode = "TX"
                )
            )
        assertThat(metafile.airportDiagrams)
            .last()
            .isEqualTo(
                ChartMetadata(
                    name = "11931AD.PDF",
                    title = "AIRPORT DIAGRAM",
                    type = ChartMetadata.Type.AIRPORT_DIAGRAM,
                    seq = 70000,
                    airportIcaoIdent = "PHNG",
                    airportIdent = "NGF",
                    airportName = "KANEOHE BAY MCAS",
                    cityName = "MOKAPU POINT",
                    procedureUid = 41566,
                    stateCode = "HI"
                )
            )

        /* Alternate minimums. */
        assertThat(metafile.alternateMinimums)
            .hasSize(1958)
            .first()
            .isEqualTo(
                ChartMetadata(
                    name = "AKALT.PDF",
                    title = "ALTERNATE MINIMUMS",
                    type = ChartMetadata.Type.ALTERNATE_MINIMUMS,
                    seq = 10200,
                    airportIcaoIdent = "PADK",
                    airportIdent = "ADK",
                    airportName = "ADAK",
                    cityName = "ADAK ISLAND",
                    stateCode = "AK"
                )
            )
        assertThat(metafile.alternateMinimums)
            .last()
            .isEqualTo(
                ChartMetadata(
                    name = "SW4ALT.PDF",
                    title = "ALTERNATE MINIMUMS",
                    type = ChartMetadata.Type.ALTERNATE_MINIMUMS,
                    seq = 10200,
                    airportIcaoIdent = "KENV",
                    airportIdent = "ENV",
                    airportName = "WENDOVER",
                    cityName = "WENDOVER",
                    stateCode = "UT"
                )
            )

        /* Approaches. */
        assertThat(metafile.approaches)
            .hasSize(13247)
            .first()
            .isEqualTo(
                ApproachMetadata(
                    chartName = "00001IL35R.PDF",
                    title = "ILS RWY 35R",
                    type = ApproachMetadata.Type.ILS,
                    runway = "35R"
                )
            )
        assertThat(metafile.approaches)
            .last()
            .isEqualTo(
                ApproachMetadata(
                    chartName = "11931TZ22.PDF",
                    title = "TACAN Z RWY 22",
                    circlingOnly = false,
                    runway = "22",
                    type = ApproachMetadata.Type.TACAN,
                    variant = 'Z'
                )
            )

        /* Approach charts. */
        assertThat(metafile.approachCharts)
            .hasSize(11483)
            .first()
            .isEqualTo(
                ChartMetadata(
                    name = "00001IL35R.PDF",
                    title = "ILS OR LOC RWY 35R",
                    type = ChartMetadata.Type.INSTRUMENT_APPROACH_PROCEDURE,
                    seq = 50750,
                    airportIcaoIdent = "KABI",
                    airportIdent = "ABI",
                    airportName = "ABILENE RGNL",
                    cityName = "ABILENE",
                    procedureUid = 2,
                    stateCode = "TX",
                    amendment = "7B",
                    amendmentDate = LocalDate.parse("2019-10-10")
                )
            )
        assertThat(metafile.approachCharts)
            .last()
            .isEqualTo(
                ChartMetadata(
                    name = "11931TZ22.PDF",
                    title = "TACAN Z RWY 22",
                    type = ChartMetadata.Type.INSTRUMENT_APPROACH_PROCEDURE,
                    seq = 56000,
                    airportIcaoIdent = "PHNG",
                    airportIdent = "NGF",
                    airportName = "KANEOHE BAY MCAS",
                    cityName = "MOKAPU POINT",
                    procedureUid = 41584,
                    stateCode = "HI"
                )
            )

        /* Departure procedures. */
        assertThat(metafile.departureProcedures)
            .hasSize(3019)
            .first()
            .isEqualTo(
                ChartMetadata(
                    name = "00007CLOUDALPHA.PDF",
                    title = "CLOUD ALPHA FOUR",
                    type = ChartMetadata.Type.DEPARTURE_PROCEDURE,
                    seq = 90100,
                    airportIcaoIdent = "KHMN",
                    airportIdent = "HMN",
                    airportName = "HOLLOMAN AFB",
                    cityName = "ALAMOGORDO",
                    procedureUid = 24071,
                    stateCode = "NM"
                )
            )
        assertThat(metafile.departureProcedures)
            .last()
            .isEqualTo(
                ChartMetadata(
                    name = "11771COPTERPRIDE_C.PDF",
                    title = "COPTER PRIDE TWO, CONT.1",
                    type = ChartMetadata.Type.DEPARTURE_PROCEDURE,
                    seq = 90200,
                    airportIcaoIdent = "KNHU",
                    airportIdent = "NHU",
                    airportName = "NORFOLK NS HELIPORT",
                    cityName = "NORFOLK",
                    procedureUid = 40317,
                    stateCode = "VA"
                )
            )

        /* Diverse vector areas. */
        assertThat(metafile.diverseVectorAreas)
            .hasSize(94)
            .first()
            .isEqualTo(
                ChartMetadata(
                    name = "AKTO.PDF",
                    title = "DIVERSE VECTOR AREA",
                    type = ChartMetadata.Type.DIVERSE_VECTOR_AREA,
                    seq = 10110,
                    airportIcaoIdent = "PAED",
                    airportIdent = "EDF",
                    airportName = "ELMENDORF AFB",
                    cityName = "ANCHORAGE",
                    stateCode = "AK"
                )
            )
        assertThat(metafile.diverseVectorAreas)
            .last()
            .isEqualTo(
                ChartMetadata(
                    name = "SW4TO.PDF",
                    title = "DIVERSE VECTOR AREA",
                    type = ChartMetadata.Type.DIVERSE_VECTOR_AREA,
                    seq = 10110,
                    airportIcaoIdent = "KSLC",
                    airportIdent = "SLC",
                    airportName = "SALT LAKE CITY INTL",
                    cityName = "SALT LAKE CITY",
                    stateCode = "UT"
                )
            )

        /* Hot spots. */
        assertThat(metafile.hotSpots)
            .hasSize(297)
            .first()
            .isEqualTo(
                ChartMetadata(
                    name = "AKHOTSPOT.PDF",
                    title = "HOT SPOT",
                    type = ChartMetadata.Type.HOT_SPOT,
                    seq = 10700,
                    airportIcaoIdent = "PAED",
                    airportIdent = "EDF",
                    airportName = "ELMENDORF AFB",
                    cityName = "ANCHORAGE",
                    stateCode = "AK"
                )
            )
        assertThat(metafile.hotSpots)
            .last()
            .isEqualTo(
                ChartMetadata(
                    name = "SW4HOTSPOT.PDF",
                    title = "HOT SPOT",
                    type = ChartMetadata.Type.HOT_SPOT,
                    seq = 10700,
                    airportIcaoIdent = "KSLC",
                    airportIdent = "SLC",
                    airportName = "SALT LAKE CITY INTL",
                    cityName = "SALT LAKE CITY",
                    stateCode = "UT"
                )
            )

        /* Information. */
        assertThat(metafile.information)
            .hasSize(15)
            .first()
            .isEqualTo(
                ChartMetadata(
                    name = "00026DPAAUP.PDF",
                    title = "RNAV DP AAUP",
                    type = ChartMetadata.Type.DEPARTURE_PROCEDURE_ATTENTION_ALL_USERS_PAGE,
                    seq = 89000,
                    airportIcaoIdent = "KATL",
                    airportIdent = "ATL",
                    airportName = "HARTSFIELD - JACKSON ATLANTA INTL",
                    cityName = "ATLANTA",
                    procedureUid = 36632,
                    stateCode = "GA"
                )
            )
        assertThat(metafile.information)
            .last()
            .isEqualTo(
                ChartMetadata(
                    name = "09077DPAAUP.PDF",
                    title = "RNAV DP AAUP",
                    type = ChartMetadata.Type.DEPARTURE_PROCEDURE_ATTENTION_ALL_USERS_PAGE,
                    seq = 89000,
                    airportIcaoIdent = "KDEN",
                    airportIdent = "DEN",
                    airportName = "DENVER INTL",
                    cityName = "DENVER",
                    procedureUid = 40045,
                    stateCode = "CO"
                )
            )

        /* Land and hold short. */
        assertThat(metafile.landAndHoldShorts)
            .hasSize(91)
            .first()
            .isEqualTo(
                ChartMetadata(
                    name = "EC1LAHSO.PDF",
                    title = "LAHSO",
                    type = ChartMetadata.Type.LAND_AND_HOLD_SHORT,
                    seq = 10600,
                    airportIcaoIdent = "KBTL",
                    airportIdent = "BTL",
                    airportName = "BATTLE CREEK EXEC AT KELLOGG FLD",
                    cityName = "BATTLE CREEK",
                    stateCode = "MI"
                )
            )
        assertThat(metafile.landAndHoldShorts)
            .last()
            .isEqualTo(
                ChartMetadata(
                    name = "SW4LAHSO.PDF",
                    title = "LAHSO",
                    type = ChartMetadata.Type.LAND_AND_HOLD_SHORT,
                    seq = 10600,
                    airportIcaoIdent = "KOGD",
                    airportIdent = "OGD",
                    airportName = "OGDEN-HINCKLEY",
                    cityName = "OGDEN",
                    stateCode = "UT"
                )
            )

        /* Obstacle departure procedures. */
        assertThat(metafile.obstacleDepartureProcedures)
            .hasSize(261)
            .first()
            .isEqualTo(
                ChartMetadata(
                    name = "00024ASTORIA.PDF",
                    title = "ASTORIA THREE (OBSTACLE)",
                    type = ChartMetadata.Type.OBSTACLE_DEPARTURE_PROCEDURE,
                    seq = 90000,
                    airportIcaoIdent = "KAST",
                    airportIdent = "AST",
                    airportName = "ASTORIA RGNL",
                    cityName = "ASTORIA",
                    procedureUid = 14740,
                    stateCode = "OR"
                )
            )
        assertThat(metafile.obstacleDepartureProcedures)
            .last()
            .isEqualTo(
                ChartMetadata(
                    name = "11931MUGGE.PDF",
                    title = "MUGGE NINE (OBSTACLE)",
                    type = ChartMetadata.Type.OBSTACLE_DEPARTURE_PROCEDURE,
                    seq = 90000,
                    airportIcaoIdent = "PHNG",
                    airportIdent = "NGF",
                    airportName = "KANEOHE BAY MCAS",
                    cityName = "MOKAPU POINT",
                    procedureUid = 41576,
                    stateCode = "HI"
                )
            )

        /* Radar minimums. */
        assertThat(metafile.radarMinimums)
            .hasSize(99)
            .first()
            .isEqualTo(
                ChartMetadata(
                    name = "AKRAD.PDF",
                    title = "RADAR MINIMUMS",
                    type = ChartMetadata.Type.RADAR_MINIMUMS,
                    seq = 10400,
                    airportIcaoIdent = "PAED",
                    airportIdent = "EDF",
                    airportName = "ELMENDORF AFB",
                    cityName = "ANCHORAGE",
                    stateCode = "AK"
                )
            )
        assertThat(metafile.radarMinimums)
            .last()
            .isEqualTo(
                ChartMetadata(
                    name = "SW4RAD.PDF",
                    title = "RADAR MINIMUMS",
                    type = ChartMetadata.Type.RADAR_MINIMUMS,
                    seq = 10400,
                    airportIcaoIdent = "KNFL",
                    airportIdent = "NFL",
                    airportName = "FALLON NAS (VAN VOORHIS FLD)",
                    cityName = "FALLON",
                    stateCode = "NV"
                )
            )

        /* Standard terminal arrivals. */
        assertThat(metafile.standardTerminalArrivals)
            .hasSize(2770)
            .first()
            .isEqualTo(
                ChartMetadata(
                    name = "00012BRRTO.PDF",
                    title = "BRRTO ONE (RNAV)",
                    type = ChartMetadata.Type.STANDARD_TERMINAL_ARRIVAL,
                    seq = 30000,
                    airportIcaoIdent = "KABQ",
                    airportIdent = "ABQ",
                    airportName = "ALBUQUERQUE INTL SUNPORT",
                    cityName = "ALBUQUERQUE",
                    procedureUid = 40610,
                    stateCode = "NM"
                )
            )
        assertThat(metafile.standardTerminalArrivals)
            .last()
            .isEqualTo(
                ChartMetadata(
                    name = "09155NASCR.PDF",
                    title = "NASCR FOUR",
                    type = ChartMetadata.Type.STANDARD_TERMINAL_ARRIVAL,
                    seq = 30000,
                    airportIcaoIdent = "KRUQ",
                    airportIdent = "RUQ",
                    airportName = "MID-CAROLINA RGNL",
                    cityName = "SALISBURY",
                    procedureUid = 15832,
                    stateCode = "NC"
                )
            )

        /* Takeoff minimums. */
        assertThat(metafile.takeoffMinimums)
            .hasSize(3109)
            .first()
            .isEqualTo(
                ChartMetadata(
                    name = "AKTO.PDF",
                    title = "TAKEOFF MINIMUMS",
                    type = ChartMetadata.Type.TAKEOFF_MINIMUMS,
                    seq = 10100,
                    airportIcaoIdent = "PADK",
                    airportIdent = "ADK",
                    airportName = "ADAK",
                    cityName = "ADAK ISLAND",
                    stateCode = "AK"
                )
            )
        assertThat(metafile.takeoffMinimums)
            .last()
            .isEqualTo(
                ChartMetadata(
                    name = "SW4TO.PDF",
                    title = "TAKEOFF MINIMUMS",
                    type = ChartMetadata.Type.TAKEOFF_MINIMUMS,
                    seq = 10100,
                    airportIcaoIdent = "KENV",
                    airportIdent = "ENV",
                    airportName = "WENDOVER",
                    cityName = "WENDOVER",
                    stateCode = "UT"
                )
            )
    }

    companion object {
        private const val SEGMENT_RESOURCE = "file:///\${CINIRA_TEST_DATASET_ROOT}/faa-dtpp/DDTPPE_240418.zip"
    }
}