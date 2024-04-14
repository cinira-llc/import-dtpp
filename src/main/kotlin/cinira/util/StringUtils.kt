package cinira.util

import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.text.Charsets.UTF_8

/**
 * Replace a prefix with a given replacement string. If the prefix is not present, the original string is returned
 * unmodified.
 *
 * @param prefix the prefix.
 * @param replacement the replacement for the prefix.
 * @return [String]
 */
fun String.replacePrefix(prefix: String, replacement: String) =
    if (!startsWith(prefix)) {
        this
    } else {
        replacement + removePrefix(prefix)
    }

fun String.urlDecodeUtf8(): String =
    URLDecoder.decode(this, UTF_8)

fun String.urlEncodeUtf8(): String =
    URLEncoder.encode(this, UTF_8)
