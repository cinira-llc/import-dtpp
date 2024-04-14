package cinira.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

/**
 * [UriUtilsTest] provides unit test coverage for `UriExt.kt`.
 */
class UriUtilsTest {

    @Test
    fun `urlEncodePathSegments() encodes path segments`() {
        assertThat(urlEncodePathSegments("test-bucket/prefix with spaces/file with spaces.txt"))
            .isEqualTo("test-bucket/prefix+with+spaces/file+with+spaces.txt")
    }

    @Test
    fun `URI_replaceScheme() replaces the scheme for a simple URI`() {
        assertThat(URI.create("s3://test-bucket/prefix/file.txt").replaceScheme("blob"))
            .isEqualTo(URI.create("blob://test-bucket/prefix/file.txt"))
    }

    @Test
    fun `URI_replaceScheme() replaces the scheme for a URI including escapes`() {
        assertThat(URI.create("s3://test-bucket/prefix%20with%20spaces/file.txt").replaceScheme("blob"))
            .isEqualTo(URI.create("blob://test-bucket/prefix%20with%20spaces/file.txt"))
    }

    @Test
    fun `URI_replaceScheme() replaces the scheme for a URI including escaped escapes`() {
        assertThat(URI.create("s3://test-bucket/prefix%2520with%2520spaces/file.txt").replaceScheme("blob"))
            .isEqualTo(URI.create("blob://test-bucket/prefix%2520with%2520spaces/file.txt"))
    }

    @Test
    fun `URI_replaceScheme() replaces the scheme for a URI including spaces`() {
        assertThat(URI.create("s3://test-bucket/prefix+with+spaces/file.txt").replaceScheme("blob"))
            .isEqualTo(URI.create("blob://test-bucket/prefix+with+spaces/file.txt"))
    }

    @Test
    fun `URI_baseName returns the correct value when there is an extension`() {
        assertThat(URI.create("https://google.com/path/to/some-file.txt.gz").baseName).isEqualTo("some-file")
    }

    @Test
    fun `URI_baseName returns the correct value when there is no extension`() {
        assertThat(URI.create("https://google.com/path/to/some-file").baseName).isEqualTo("some-file")
    }

    @Test
    fun `URI_baseName returns the correct value when there is no path`() {
        assertThat(URI.create("https://google.com").baseName).isEqualTo("")
    }

    @Test
    fun `URI_baseName returns the correct value`() {
        assertThat(URI.create("https://google.com/path/to/some-file.txt.gz").baseName).isEqualTo("some-file")
    }

    @Test
    fun `URI_extension returns the correct value when there is an extension`() {
        assertThat(URI.create("https://google.com/path/to/some-file.txt.gz").extension).isEqualTo("gz")
    }

    @Test
    fun `URI_extension returns the correct value when there is no extension`() {
        assertThat(URI.create("https://google.com/path/to/some-file").extension).isEqualTo("")
    }

    @Test
    fun `URI_extensions returns the correct value when there are extensions`() {
        assertThat(URI.create("https://google.com/path/to/some-file.txt.gz").extensions).containsExactly("txt", "gz")
    }

    @Test
    fun `URI_extensions returns the correct value when there are no extensions`() {
        assertThat(URI.create("https://google.com/path/to/some-file").extensions).isEmpty()
    }

    @Test
    fun `URI_directory returns the correct value for a file in a directory`() {
        assertThat(URI.create("https://google.com/path/to/some-file.txt.gz").directory).isEqualTo("/path/to")
    }

    @Test
    fun `URI_directory returns the correct value for a file at the root`() {
        assertThat(URI.create("https://google.com/some-file.txt.gz").directory).isEqualTo("/")
    }

    @Test
    fun `URI_filename returns the correct value for a file in a directory`() {
        assertThat(URI.create("https://google.com/path/to/some-file.txt.gz").filename).isEqualTo("some-file.txt.gz")
    }

    @Test
    fun `URI_filename returns the correct value for a file at the root`() {
        assertThat(URI.create("https://google.com/some-file.txt.gz").filename).isEqualTo("some-file.txt.gz")
    }

    @Test
    fun `URI_filename returns the correct value when there is no path`() {
        assertThat(URI.create("https://google.com").filename).isEqualTo("")
    }
}