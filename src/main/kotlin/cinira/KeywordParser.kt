package cinira

import cinira.model.MediaEntry
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

/**
 * [KeywordParser] parses keyword data out of a PDF document and folds it into a [MediaEntry] object.
 */
internal class KeywordParser {

    fun parse(entry: MediaEntry, pdf: PDDocument): MediaEntry =
        PDFTextStripper().run {
            sortByPosition = true
            getText(pdf)
                .lines()
                .flatMap { line -> line.split(".") }
                .filter(String::isNotBlank)
                .distinct()
                .fold(entry) { acc, line ->
                    val words = line.uppercase().split(nonAlpha)
                    keywords.filter(words::contains)
                        .fold(acc) { accKeywords, keyword ->
                            val cleaned = line.removePrefix("A ")
                                .removePrefix("T ")
                                .replace(whitespace, " ")
                                .trim()
                            accKeywords.addKeyword(keyword, cleaned)
                        }
                }
        }

    /**
     * Get a derived [MediaEntry] instance with an added keyword entry.
     *
     * @receiver [MediaEntry]
     * @param keyword the matched keyword.
     * @param run the text run in which the keyword was matched.
     * @return [MediaEntry]
     */
    private fun MediaEntry.addKeyword(keyword: String, run: String) =
        copy(
            keyword = this.keyword + (keyword to ((this.keyword[keyword] ?: emptySet()) + run).sorted().toSet())
        )

    companion object {
        private val keywords = listOf(
            "ACTIVITY",
            "ADF",
            "ALERT",
            "CLIMBING",
            "DME",
            "ENTRY",
            "EXCEED",
            "EXPECT",
            "LPV",
            "MUST",
            "NA",
            "NIGHT",
            "OBSTACLES",
            "RADAR",
            "REQUIRED"
        )
        private val nonAlpha = Regex("[^A-Z]+")
        private val whitespace = Regex("\\s+")
    }
}

