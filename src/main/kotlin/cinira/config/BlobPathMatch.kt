package cinira.config

import java.net.URI

/**
 * [BlobPathMatch] holds a `prefix` path, and an optional regular expression `pattern`, used to match objects in blob
 * storage.
 *
 * Blob storage products do not support wildcards or patterns, but they (usually?) support listing objects matching a
 * static prefix. This combines such a prefix for "server side" filtering along with an optional regex to apply to the
 * matches on the client side to arrive at a final list.
 */
data class BlobPathMatch(

    /**
     * Matching prefix, identifying the bucket and key prefix.
     */
    var prefix: String,

    /**
     * Optional pattern to further filter the list of objects matching [prefix].
     */
    var pattern: Regex? = null
) {
    /**
     * Determine whether a blob path matches the prefix and/or pattern in this instance.
     *
     * @param uri the `blob:` URI to check.
     * @return [Boolean]
     */
    fun matches(uri: URI) =
        uri.schemeSpecificPart.removePrefix("//").let { path ->
            path.startsWith(prefix) && false != pattern?.containsMatchIn(path)
        }

    /**
     * Given a source `path` that matches the prefix and/or pattern in this instance, *resolve* a derived path described
     * by a `format`, which may contain `{..}` replacement tokens (note: no dollar sign preceding the opening brace.)
     *
     * In addition to any `variables` in a caller-provided map, if a `pattern` is configured in this instance, exposes
     * capturing subgroup values in `$0`, `$1`, ... `$n` for substitution into the resolved path.
     *
     * It is assumed that a prior invocation of [matches] on the source `path` returned `true`; if this is not the case
     * and the path does not match, an [IllegalStateException] is thrown.
     *
     * @param uri the source path, which must be known to match the prefix and/or pattern in this instance.
     * @param format the target path format.
     * @param variables the variables to expose for `{..}` substitutions in the `format` path.
     * @return [URI]
     * @throws IllegalArgumentException if the source `path` does not match the prefix and/or pattern in this instance.
     */
    fun resolve(uri: URI, format: String, variables: Map<String, Any> = emptyMap()): URI =
        uri.schemeSpecificPart.removePrefix("//").let { path ->
            if (!path.startsWith(prefix)) {
                throw IllegalArgumentException("Prefix does not match.")
            }
            pattern?.find(path)?.let { match ->
                match.groupValues.foldIndexed(emptyMap<String, String>()) { index, acc, value ->
                    acc + ("\$$index" to value)
                }
            }?.let { groupVariables ->
                val allVariables = groupVariables + variables
                val formatted = token.replace(format) { match ->
                    allVariables[match.groupValues[1]]!!.toString()
                }
                URI.create("blob://$formatted")
            } ?: throw IllegalArgumentException("Pattern does not match.")
        }

    companion object {
        private val token = Regex("\\{([^}]+)}")
    }
}
