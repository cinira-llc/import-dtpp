package cinira.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate

/**
 * [ChartMetadata] holds basic information on a single chart from a DTPP segment archive.
 */
data class ChartMetadata(

    /**
     * Name of the chart file, such as `00106IL31L.PDF`.
     */
    val name: String,

    /**
     * Human-readable procedure title.
     */
    val title: String,

    /**
     * General chart type.
     */
    val type: Type,

    /**
     * Original `chartseq` value, can be used to determine some characteristics of approach procedures, such as navaid
     * type, convergence, helicopter classification, etc.
     *
     * @see `doc/vendor/faa-dtpp.md`
     */
    val seq: Int,

    /**
     * Airport ICAO identifier.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val airportIcaoIdent: String?,

    /**
     * Airport identifier.
     */
    val airportIdent: String,

    /**
     * Airport name.
     */
    val airportName: String,

    /**
     * City name.
     */
    val cityName: String,

    /**
     * 2-letter state code.
     */
    val stateCode: String,

    /**
     * For approach, arrival, and departure procedures, identifier for cross-reference to the CIFP.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val procedureUid: Int? = null,

    /**
     * Amendment number.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val amendment: String? = null,

    /**
     * Amendment date.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val amendmentDate: LocalDate? = null,
) {

    /**
     * [Type]
     */
    enum class Type {
        AIRPORT_DIAGRAM,
        ALTERNATE_MINIMUMS,
        INSTRUMENT_APPROACH_PROCEDURE,
        DEPARTURE_PROCEDURE,
        DEPARTURE_PROCEDURE_ATTENTION_ALL_USERS_PAGE,
        DIVERSE_VECTOR_AREA,
        HOT_SPOT,
        LAND_AND_HOLD_SHORT,
        OBSTACLE_DEPARTURE_PROCEDURE,
        RADAR_MINIMUMS,
        TAKEOFF_MINIMUMS,
        STANDARD_TERMINAL_ARRIVAL;

        companion object {
            fun fromCodeAndSeq(code: String, seq: Int) =
                when (code) {
                    "DAU" -> DEPARTURE_PROCEDURE_ATTENTION_ALL_USERS_PAGE
                    "APD" -> AIRPORT_DIAGRAM
                    "DP" -> DEPARTURE_PROCEDURE
                    "HOT" -> HOT_SPOT
                    "IAP" -> INSTRUMENT_APPROACH_PROCEDURE
                    "LAH" -> LAND_AND_HOLD_SHORT
                    "MIN" -> when (seq) {
                        10100 -> TAKEOFF_MINIMUMS
                        10110 -> DIVERSE_VECTOR_AREA
                        10200 -> ALTERNATE_MINIMUMS
                        10400 -> RADAR_MINIMUMS
                        else -> throw IllegalArgumentException()
                    }

                    "ODP" -> OBSTACLE_DEPARTURE_PROCEDURE
                    "STAR" -> STANDARD_TERMINAL_ARRIVAL
                    else -> throw IllegalArgumentException()
                }
        }
    }
}
