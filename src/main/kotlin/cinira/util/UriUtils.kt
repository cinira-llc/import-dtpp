package cinira.util

import java.net.URI
import java.net.URLEncoder

/**
 * Get the file basename portion of the URI path. This includes everything after the last `/` and before (and not
 * including) the first `.`. If the path is empty, returns an empty string.
 */
val URI.baseName: String
    get() = filename.substringBefore('.')

/**
 * Get the *directory path* portion of the URI path. This includes the leading slash and everything up to but *not*
 * including the slash before the filename. If there is no path, ie. the filename is at the root, `/` is returned.
 */
val URI.directory: String
    get() = "/" + path.substringBeforeLast('/').removePrefix("/")

/**
 * Get the *last* filename extension, not including the leading dot. Returns an empty string if there is no extension.
 */
val URI.extension: String
    get() = extensions.lastOrNull() ?: ""

/**
 * Get all filename extensions, in order from first to last, not including the leading dots. Returns an empty list if
 * there are no extensions.
 */
val URI.extensions: List<String>
    get() = if (!filename.contains('.')) {
        emptyList()
    } else {
        filename.substringAfter('.').split('.')
    }

/**
 * Get the filename, which is everything after the last `/` in the path. If the path is empty, returns an empty string.
 */
val URI.filename: String
    get() = path.substringAfterLast('/')

/**
 * Replace the scheme portion, *only*, of a URI with a given value.
 *
 * @param newScheme the replacement scheme, must not include the trailing `:`.
 * @return [URI]
 */
fun URI.replaceScheme(newScheme: String): URI =
    if (scheme == newScheme) {
        this
    } else {
        URI.create(newScheme + toString().removePrefix(scheme))
    }

/**
 * Split a path by `/` delimiters and *separately* URL encode each segment, meaning that the delimiters themselves are
 * *not* encoded.
 *
 * @param path the path whose segments are to be encoded.
 * @return [String]
 */
fun urlEncodePathSegments(path: String) =
    path.split('/').let { segments ->
        segments.joinToString("/") { segment ->
            URLEncoder.encode(segment, Charsets.UTF_8)
        }
    }
