package cinira

import cinira.model.ApproachMetadata
import cinira.model.ChartMetadata
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
        val metafile = segment.inputStream.let(::ZipInputStream).use { zip ->
            generateSequence(zip::getNextEntry)
                .filter { entry -> "d-TPP_Metafile.xml" == entry.name }
                .map { parser.parse(CloseShieldInputStream.wrap(zip)) }
                .single()
        }

        /* Cycle attributes. */
        assertThat(metafile.cycle).isEqualTo(2208)
        assertThat(metafile.effectiveStartDateTime).isEqualTo("2022-08-11T09:01:00Z")
        assertThat(metafile.effectiveEndDateTime).isEqualTo("2022-09-08T09:01:00Z")

        /* Airport diagrams. */
        assertThat(metafile.airportDiagrams)
            .hasSize(821)
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
                    stateCode = "TX"
                )
            )
        assertThat(metafile.airportDiagrams)
            .last()
            .isEqualTo(
                ChartMetadata(
                    name = "11234AD.PDF",
                    title = "AIRPORT DIAGRAM",
                    type = ChartMetadata.Type.AIRPORT_DIAGRAM,
                    seq = 70000,
                    airportIcaoIdent = "KXWA",
                    airportIdent = "XWA",
                    airportName = "WILLISTON BASIN INTL",
                    cityName = "WILLISTON",
                    stateCode = "ND"
                )
            )

        /* Alternate minimums. */
        assertThat(metafile.alternateMinimums)
            .hasSize(1905)
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
            .hasSize(13331)
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
                    chartName = "DELETED_JOB.PDF",
                    title = "VOR/DME-A",
                    circlingOnly = true,
                    type = ApproachMetadata.Type.VOR_DME,
                    variant = 'A'
                )
            )

        /* Approach charts. */
        assertThat(metafile.approachCharts)
            .hasSize(11561)
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
                    stateCode = "TX",
                    amendment = "7B",
                    amendmentDate = LocalDate.parse("2019-10-10")
                )
            )
        assertThat(metafile.approachCharts)
            .last()
            .isEqualTo(
                ChartMetadata(
                    name = "DELETED_JOB.PDF",
                    title = "VOR/DME-A",
                    type = ChartMetadata.Type.INSTRUMENT_APPROACH_PROCEDURE,
                    seq = 55125,
                    airportIcaoIdent = "KDUX",
                    airportIdent = "DUX",
                    airportName = "MOORE COUNTY",
                    cityName = "DUMAS",
                    stateCode = "TX",
                    amendment = "6A",
                    amendmentDate = LocalDate.parse("2015-07-23")
                )
            )

        /* Departure procedures. */
        assertThat(metafile.departureProcedures)
            .hasSize(2991)
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
                    stateCode = "NM"
                )
            )
        assertThat(metafile.departureProcedures)
            .last()
            .isEqualTo(
                ChartMetadata(
                    name = "11771COPTERPRIDE_C.PDF",
                    title = "COPTER PRIDE ONE, CONT.1",
                    type = ChartMetadata.Type.DEPARTURE_PROCEDURE,
                    seq = 90200,
                    airportIcaoIdent = "KNHU",
                    airportIdent = "NHU",
                    airportName = "NORFOLK NS HELIPORT",
                    cityName = "NORFOLK",
                    stateCode = "VA"
                )
            )

        /* Diverse vector areas. */
        assertThat(metafile.diverseVectorAreas)
            .hasSize(92)
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
            .hasSize(302)
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
                    stateCode = "CO"
                )
            )

        /* Land and hold short. */
        assertThat(metafile.landAndHoldShorts)
            .hasSize(93)
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
            .hasSize(230)
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
                    stateCode = "OR"
                )
            )
        assertThat(metafile.obstacleDepartureProcedures)
            .last()
            .isEqualTo(
                ChartMetadata(
                    name = "11796MARVN.PDF",
                    title = "MARVN ONE (OBSTACLE) (RNAV)",
                    type = ChartMetadata.Type.OBSTACLE_DEPARTURE_PROCEDURE,
                    seq = 90000,
                    airportIcaoIdent = "PAKX",
                    airportIdent = "05K",
                    airportName = "WILDER RUNWAY",
                    cityName = "PORT ALSWORTH",
                    stateCode = "AK"
                )
            )

        /* Radar minimums. */
        assertThat(metafile.radarMinimums)
            .hasSize(108)
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
            .hasSize(2753)
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
                    stateCode = "NC"
                )
            )

        /* Takeoff minimums. */
        assertThat(metafile.takeoffMinimums)
            .hasSize(3084)
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
        private const val SEGMENT_RESOURCE = "file:///\${CINIRA_TEST_DATASET_ROOT}/faa-dtpp/DDTPPE_220811.zip"
    }
}