package cinira

import cinira.model.MediaEntry
import org.apache.commons.io.input.CloseShieldInputStream
import org.apache.pdfbox.Loader
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import java.util.zip.ZipInputStream

/**
 * [KeywordParserTest] provides unit test coverage for [KeywordParser].
 *
 * Requires a known test segment (see [SEGMENT_RESOURCE]) to be available via the `CINIRA_TEST_DATASET_ROOT` environment
 * variable.
 */
@EnabledIfEnvironmentVariable(
    named = "CINIRA_TEST_DATASET_ROOT",
    matches = ".+",
    disabledReason = "Test datasets not found"
)
@SpringJUnitConfig(classes = [KeywordParserTest::class])
internal class KeywordParserTest {

    @Test
    fun `parse() parses a known segment correctly`(@Value(SEGMENT_RESOURCE) segment: Resource) {
        assertThat(segment.exists()).isTrue
        val keywords = KeywordParser()
        val charts = segment.inputStream.let(::ZipInputStream).use { zip ->
            generateSequence(zip::getNextEntry)
                .filter { entry ->
                    val name = entry.name.lowercase()
                    name.endsWith(".pdf") && !name.startsWith("readme") && !name.contains("/")
                }
                .map { entry ->
                    val bytes = CloseShieldInputStream.wrap(zip).readAllBytes()
                    val pdf = Loader.loadPDF(bytes)
                    val media = MediaEntry(
                        name = entry.name,
                        type = MediaEntry.Type.CHART,
                        contentType = "application/pdf",
                        size = bytes.size.toLong(),
                    )
                    keywords.parse(media, pdf)
                }
                .sortedBy(MediaEntry::name)
                .toList()
        }
        assertThat(charts)
            .hasSize(4178)
            .first()
            .isEqualTo(
                MediaEntry(
                    name = "00001AD.PDF",
                    type = MediaEntry.Type.CHART,
                    contentType = "application/pdf",
                    size = 145118L,
                    keyword = mapOf(
                        "ALERT" to setOf(
                            "CAUTION: BE ALERT TO RUNWAY CROSSING CLEARANCES"
                        ),
                        "REQUIRED" to setOf(
                            "READBACK OF ALL RUNWAY HOLDING INSTRUCTIONS IS REQUIRED"
                        )
                    )
                )
            )
        assertThat(charts)
            .last()
            .isEqualTo(
                MediaEntry(
                    name = "00379V12.PDF",
                    type = MediaEntry.Type.CHART,
                    contentType = "application/pdf",
                    size = 336273L,
                    keyword = mapOf(
                        "CLIMBING" to setOf(
                            "4 to 1000 then climbing left turn"
                        ),
                        "NA" to setOf(
                            "Night Landing Rwy 2 NA"
                        ),
                        "NIGHT" to setOf(
                            "Night Landing Rwy 2 NA"
                        )
                    )
                )
            )
    }

    companion object {
        private const val SEGMENT_RESOURCE = "file:///\${CINIRA_TEST_DATASET_ROOT}/faa-dtpp/DDTPPA_240418.zip"
    }
}