package cinira

import cinira.model.ChartDetails
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import java.io.InputStream

/**
 * [ChartParser] parses a [ChartDetails] out of a chart PDF.
 */
internal class ChartParser {

    /**
     * Parse a chart PDF.
     *
     * @param name the name of the PDF file within the segment archive.
     * @param content the stream from which to read the PDF content.
     * @return [ChartDetails]
     */
    fun parse(name: String, content: InputStream): ChartDetails =
        Loader.loadPDF(content.readAllBytes()).use { pdf ->
            PDFTextStripper().run {
                sortByPosition = true
                getText(pdf)
                    .lines()
                    .distinct()
                    .fold(ChartDetails(name)) { acc, line ->
                        val normalized = whitespace.replace(line, " ")
                        keyphrases.filter(normalized::contains)
                            .fold(acc) { accPhrase, keyPhrase ->
                                accPhrase.addKeyphrase(keyPhrase, normalized)
                            }
                    }
            }.run {
                copy(
                    keyphrases = keyphrases.sorted().toSet(),
                    keyphraseRuns = keyphraseRuns.sorted().toSet()
                )
            }
        }

    /**
     * Get a derived [ChartDetails] instance with an added keyphrase entry.
     *
     * @receiver [ChartDetails]
     * @param phrase the matched keyphrase.
     * @param run the text run in which the keyphrase was matched.
     * @return [ChartDetails]
     */
    private fun ChartDetails.addKeyphrase(phrase: String, run: String) =
        copy(
            keyphrases = keyphrases + phrase,
            keyphraseRuns = keyphraseRuns + run
        )

    companion object {
        private val keyphrases = listOf(
            "ACTIVITY",
            "ADF",
            "CIRCLING NA",
            "CLIMBING",
            "DME",
            "EXPECT",
            "LPV",
            "MUST BE",
            "OBSTACLES",
            "PROCEDURE ENTRY",
            "PROCEDURE NA",
            "RADAR",
            "REQUIRED"
        )
        private val whitespace = Regex("\\s+")
    }
}

