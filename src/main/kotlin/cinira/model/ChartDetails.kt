package cinira.model

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * [ChartDetails] holds the name of a chart PDF and any details which were parsed out of the PDF itself.
 */
data class ChartDetails(

    /**
     * Name of the chart file, such as `00106IL31L.PDF`.
     */
    val name: String,

    /**
     * Keyphrases which were found in the text of the chart.
     *
     * For each keyphrase here, at least one line will be present in [keyphraseRuns] which *contains* that phrase.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val keyphrases: Set<String> = emptySet(),

    /**
     * Runs of text which contained one or more keyphrases in the text of the chart.
     *
     * For each run of text here, at least one keyphrase will be present in [keyphrases] which it *contains.*
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val keyphraseRuns: Set<String> = emptySet()
)
