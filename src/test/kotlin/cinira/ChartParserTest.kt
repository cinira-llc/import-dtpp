package cinira

import cinira.model.ChartDetails
import org.apache.commons.io.input.CloseShieldInputStream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.zip.ZipInputStream

/**
 * [ChartParserTest] provides unit test coverage for [ChartParser].
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
internal class ChartParserTest {

    @Test
    fun `parse() parses a known segment correctly`(@Value(SEGMENT_RESOURCE) segment: Resource) {
        assertThat(segment.exists()).isTrue
        val parser = ChartParser()
        val charts = segment.inputStream.let(::ZipInputStream).use { zip ->
            generateSequence(zip::getNextEntry)
                .filter { entry ->
                    val name = entry.name.lowercase()
                    name.endsWith(".pdf") && !name.startsWith("readme") && !name.contains("/")
                }
                .map { entry ->
                    parser.parse(entry.name, CloseShieldInputStream.wrap(zip))
                }
                .sortedBy(ChartDetails::name)
                .toList()
        }
        assertThat(charts)
            .hasSize(4203)
            .first()
            .isEqualTo(
                ChartDetails(
                    name = "00001AD.PDF",
                    keyphrases = setOf("REQUIRED"),
                    keyphraseRuns = setOf("READBACK OF ALL RUNWAY HOLDING INSTRUCTIONS IS REQUIRED.")
                )
            )
        assertThat(charts)
            .last()
            .isEqualTo(
                ChartDetails(
                    name = "00379V12.PDF",
                    keyphrases = emptySet(),
                    keyphraseRuns = emptySet()
                )
            )
    }

    companion object {
        private const val SEGMENT_RESOURCE = "file:///\${CINIRA_TEST_DATASET_ROOT}/faa-dtpp/DDTPPA_220811.zip"
    }
}