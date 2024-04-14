package cinira.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cinira.import-dtpp")
data class ImportDtppProperties(

    /**
     * Destination blob paths for different artifact types.
     */
    var destinations: DestinationsProperties,

    /**
     * Match which locates incoming FAA DTPP segments in blob storage.
     */
    var incoming: BlobPathMatch,
) {
    /**
     * [DestinationsProperties] holds blob path formats for building the destination paths for DTPP segment artifacts,
     * such as charts, chart diffs, and thumbnails.
     */
    data class DestinationsProperties(

        /**
         * Chart target path, which may contain `{..}` substitution tokens per [BlobPathMatch].
         */
        var charts: String,

        /**
         * Chart diff target path, which may contain `{..}` substitution tokens per [BlobPathMatch].
         */
        var diffs: String,

        /**
         * Cycle metafile target path, which may contain `{..}` substitution tokens per [BlobPathMatch].
         */
        var metafile: String,

        /**
         * Segment index target path, which may contain `{..}` substitution tokens per [BlobPathMatch].
         */
        var segments: String,
    )
}
