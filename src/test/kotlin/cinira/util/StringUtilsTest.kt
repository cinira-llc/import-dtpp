package cinira.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * [StringUtilsTest] provides unit test coverage for `StringUtils.kt`.
 */
internal class StringUtilsTest {

    @Test
    fun `String_replacePrefix() replaces nothing if the prefix is not found`() {
        assertThat("s3://test-bucket/prefix/file.txt".replacePrefix("blob:", "s3:"))
            .isEqualTo("s3://test-bucket/prefix/file.txt")
    }

    @Test
    fun `String_replacePrefix() replaces a prefix that is found`() {
        assertThat("blob://test-bucket/prefix/file.txt".replacePrefix("blob:", "s3:"))
            .isEqualTo("s3://test-bucket/prefix/file.txt")
    }

    @Test
    fun `String_replacePrefix() replaces a prefix that matches the entire string`() {
        assertThat("blob:".replacePrefix("blob:", "s3:")).isEqualTo("s3:")
    }
}