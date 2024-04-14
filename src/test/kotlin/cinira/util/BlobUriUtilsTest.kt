package cinira.util

import cinira.support.blob.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException
import java.net.URI

/**
 * [BlobUriUtilsTest] provides unit test coverage for `BlobUriExt.kt`.
 */
class BlobUriUtilsTest {

    @Test
    fun `URI_blobPath throws for non-blob URI`() {
        assertThrows<IllegalStateException> {
            URI.create("http://www.google.com").blobPath
        }
    }

    @Test
    fun `URI_blobPath returns the correct path`() {
        assertThat(URI.create("blob://test-bucket/test-prefix/test-blob.txt").blobPath)
            .isEqualTo("test-bucket/test-prefix/test-blob.txt")
    }

    @Test
    fun `URI_blobPathMatches returns true for a prefix only match`() {
        val uri = URI.create("blob://test-bucket/test-prefix/test-blob.txt")
        assertThat(uri.blobPathMatches("test-bucket/test-prefix/test-")).isTrue
    }

    @Test
    fun `URI_blobPathMatches returns false for a prefix only non-match`() {
        val uri = URI.create("blob://test-bucket/test-prefix/test-blob.txt")
        assertThat(uri.blobPathMatches("test-bucket/test-prefix/non-match-")).isFalse
    }

    @Test
    fun `URI_blobPathMatches returns true for a prefix and pattern match`() {
        val uri = URI.create("blob://test-bucket/test-prefix/test-blob.txt")
        assertThat(uri.blobPathMatches("test-bucket/test-prefix/test-", Regex("\\.txt$"))).isTrue
    }

    @Test
    fun `URI_blobPathMatches returns false for a prefix and pattern non-match`() {
        val uri = URI.create("blob://test-bucket/test-prefix/test-blob.txt")
        assertThat(uri.blobPathMatches("test-bucket/test-prefix/test-", Regex("\\.non-match$"))).isFalse
    }

    @Test
    fun `blobUri() returns a blob URI for combined bucket and key`() {
        assertThat(blobUri("test-bucket/test-prefix/test-blob.txt"))
            .isEqualTo(URI.create("blob://test-bucket/test-prefix/test-blob.txt"))
    }

    @Test
    fun `blobUri() returns a blob URI for separate bucket and key`() {
        assertThat(blobUri("test-bucket", "test-prefix/test-blob.txt"))
            .isEqualTo(URI.create("blob://test-bucket/test-prefix/test-blob.txt"))
    }

    @Test
    fun `blobUri() throws for an invalid bucket`() {
        assertThrows<IllegalArgumentException> {
            blobUri("test bucket", "test-prefix/test-blob.txt")
        }
    }

    @Test
    fun `URI_bucket throws for non-blob URI`() {
        assertThrows<IllegalStateException> {
            URI.create("http://www.google.com").bucket
        }
    }

    @Test
    fun `URI_bucket returns the correct bucket`() {
        assertThat(URI.create("blob://test-bucket/test-prefix/test-blob.txt").bucket)
            .isEqualTo("test-bucket")
    }

    @Test
    fun `URI_key throws for non-blob URI`() {
        assertThrows<IllegalStateException> {
            URI.create("http://www.google.com").key
        }
    }

    @Test
    fun `URI_key returns the correct key`() {
        assertThat(URI.create("blob://test-bucket/test-prefix/test-blob.txt").key)
            .isEqualTo("test-prefix/test-blob.txt")
    }
}