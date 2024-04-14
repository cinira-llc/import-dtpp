package cinira.support.blob

import java.net.URI

/**
 * Get the bucket and key from a `blob:` URI. This is a slash-delimited concatenation of the bucket and key, with no
 * leading slash.
 */
val URI.blobPath: String
    get() = requireBlobPath(this)

/**
 * Get the bucket from a `blob:` URI.
 */
val URI.bucket: String
    get() = requireBlobPath(this).split('/').first()

/**
 * Get the key from a `blob:` URI. This does not contain a leading slash.
 */
val URI.key: String
    get() = requireBlobPath(this).substringAfter('/')

/**
 * Determine whether a `blob:` URI matches a given prefix (including bucket name) and, optionally, pattern. The prefix
 * should not contain a leading slash; if a `pattern` is provided, it is matched against the entire blob path.
 *
 * @param prefix the prefix.
 * @param pattern the pattern, optional.
 * @return [Boolean]
 */
fun URI.blobPathMatches(prefix: String, pattern: Regex? = null) =
    blobPath.let { path ->
        path.startsWith(prefix) && false != pattern?.containsMatchIn(path)
    }

/**
 * Build a `blob:` URI from a bucket name and key.
 *
 * @param bucket the bucket.
 * @param key the key.
 * @return [URI]
 */
fun blobUri(bucket: String, key: String): URI {
    requireValidBucket(bucket)
    return URI.create("blob://$bucket/$key")
}

/**
 * Build a `blob:` URI from a bucket name and key in a slash-delimited concatenated string.
 *
 * @param bucketAndKey the bucket and key, delimited by `/`.
 * @return [URI]
 */
fun blobUri(bucketAndKey: String): URI {
    val index = bucketAndKey.indexOf('/')
    if (-1 == index) {
        throw IllegalArgumentException("Invalid bucket and key.")
    }
    return blobUri(bucketAndKey.substring(0, index), bucketAndKey.substring(index + 1))
}

/**
 * Get the bucket and key, as a single slash-delimited string, from a `blob:` URI.
 *
 * @param uri the `blob:` URI.
 * @throws IllegalStateException if the URI scheme is not `blob`.
 */
private fun requireBlobPath(uri: URI): String =
    if ("blob" != uri.scheme) {
        throw IllegalStateException("Not a blob: URI.")
    } else {
        uri.schemeSpecificPart.removePrefix("//")
    }

/**
 * Validate a bucket and key.
 *
 * @throws IllegalArgumentException if the bucket or key is invalid.
 */
private fun requireValidBucket(bucket: String) {
    if (invalidBucket(bucket)) {
        throw IllegalArgumentException("Invalid bucket.")
    }
}

private val invalidBucket = Regex("^[a-z]([a-z0-9-]*[a-z0-9])?$").toPattern().asMatchPredicate().negate()::test
