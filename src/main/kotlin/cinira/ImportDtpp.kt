package cinira

import cinira.util.get
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.S3Event
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.s3.S3Client
import java.lang.System.getenv
import java.net.URI

@Suppress("unused")
class ImportDtpp internal constructor(
    private val client: S3Client,
    private val importer: ImportDtppSegment
) : RequestHandler<S3Event, List<URI>> {
    constructor() : this(
        client = S3Client.create(),
        targetBucket = getenv("TARGET_BUCKET")
    )

    internal constructor(client: S3Client, targetBucket: String) : this(
        client = client,
        importer = ImportDtppSegmentImpl(
            client = client,
            targetBucket = targetBucket
        )
    )

    override fun handleRequest(event: S3Event, ctx: Context): List<URI> {
        return event.records.map { record ->
            val bucket = record.s3.bucket.name
            val key = record.s3.`object`.key
            log.debug("Processing dTPP segment at bucket [{}] key [{}].", bucket, key)
            val resource = client.get(bucket, key)
            importer.execute(resource).also {
                client.deleteObject { delete -> delete.bucket(bucket).key(key) }
            }
        }.toList()
    }

    companion object {
        private val log = LoggerFactory.getLogger(ImportDtpp::class.java)
    }
}
