package cinira.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI

/**
 * [BlobPathMatchTest] provides unit test coverage for [BlobPathMatch].
 */
internal class BlobPathMatchTest {

    @Test
    fun `matches() returns false for a prefix-only instance that does not match the bucket`() {
        val match = BlobPathMatch(
            prefix = "test-bucket/parent/child/"
        )
        assertThat(match.matches(URI.create("blob://other-bucket/parent/child/file.txt"))).isFalse
    }

    @Test
    fun `matches() returns false for a prefix-only instance that does not match the key`() {
        val match = BlobPathMatch(
            prefix = "test-bucket/parent/child/"
        )
        assertThat(match.matches(URI.create("blob://test-bucket/parent/file.txt"))).isFalse
    }

    @Test
    fun `matches() returns true for a prefix-only instance that matches`() {
        val match = BlobPathMatch(
            prefix = "test-bucket/parent/child/"
        )
        assertThat(match.matches(URI.create("blob://test-bucket/parent/child/file.txt"))).isTrue
    }

    @Test
    fun `matches() returns false for a prefix and pattern instance that does not match the prefix`() {
        val match = BlobPathMatch(
            prefix = "test-bucket/parent/child/",
            pattern = Regex("/.+\\.txt$")
        )
        assertThat(match.matches(URI.create("blob://test-bucket/parent/file.txt"))).isFalse
    }

    @Test
    fun `matches() returns false for a prefix and pattern instance that matches the prefix but not the pattern`() {
        val match = BlobPathMatch(
            prefix = "test-bucket/parent/child/",
            pattern = Regex("/.+\\.txt$")
        )
        assertThat(match.matches(URI.create("blob://test-bucket/parent/child/file.png"))).isFalse
    }

    @Test
    fun `matches() returns false for a prefix and pattern instance that matches the prefix and the pattern`() {
        val match = BlobPathMatch(
            prefix = "test-bucket/parent/child/",
            pattern = Regex("/.+\\.txt$")
        )
        assertThat(match.matches(URI.create("blob://test-bucket/parent/child/file.txt"))).isTrue
    }

    @Test
    fun `resolve() throws IllegalArgumentException if the prefix does not match`() {
        val match = BlobPathMatch(
            prefix = "test-bucket/parent/child/",
            pattern = Regex("/(.+)\\.txt$")
        )
        assertThrows<IllegalArgumentException> {
            val source = URI.create("blob://test-bucket/parent/file.txt")
            val format = "test-bucket/parent/another-child/{$1}.{ext}"
            match.resolve(source, format)
        }
    }

    @Test
    fun `resolve() throws IllegalArgumentException if the pattern does not match`() {
        val match = BlobPathMatch(
            prefix = "test-bucket/parent/child/",
            pattern = Regex("/(.+)\\.txt$")
        )
        assertThrows<IllegalArgumentException> {
            val source = URI.create("blob://test-bucket/parent/child/file.png")
            val format = "test-bucket/parent/another-child/{$1}.{ext}"
            match.resolve(source, format)
        }
    }

    @Test
    fun `resolve() resolves a match correctly`() {
        val match = BlobPathMatch(
            prefix = "test-bucket/parent/child/",
            pattern = Regex("/([^/]+)\\.txt$")
        )
        val source = URI.create("test-bucket/parent/child/file.txt")
        val format = "test-bucket/parent/another-child/{$1}.{ext}"
        assertThat(match.resolve(source, format, mapOf("ext" to "png")))
            .isEqualTo(URI.create("blob://test-bucket/parent/another-child/file.png"))
    }
}