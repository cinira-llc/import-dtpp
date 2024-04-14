package cinira

import cinira.config.ImportDtppProperties
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.S3Event
import org.slf4j.LoggerFactory.getLogger
import org.springframework.core.io.support.ResourcePatternResolver
import java.net.URI

/**
 * [ImportDtppSegmentHandler] wraps [ImportDtppSegment] to conform to the AWS Lambda API.
 */
@Suppress("unused")
internal open class ImportDtppSegmentHandler internal constructor(
    private val importer: ImportDtppSegment
) : RequestHandler<S3Event, List<URI>> {
    constructor(resolver: ResourcePatternResolver, config: ImportDtppProperties) : this(
        importer = ImportDtppSegment.create(
            resolver = resolver,
            config = config
        )
    )

    override fun handleRequest(event: S3Event, context: Context) =
        event.records.mapNotNull { record ->
            val name = record.eventName
            if (name !in uploadEvents) {
                log.debug("Skipping record with unsupported event [{}].", event)
                null
            } else {
                val s3 = record.s3;
                val path = URI.create("s3:${s3.bucket.name}/${s3.`object`.key}")
                importer.execute(path)
            }
        }

    companion object {
        private val log = getLogger(ImportDtppSegmentHandler::class.java)
        private val uploadEvents = setOf("ObjectCreated:Post", "ObjectCreated:Put")
    }
}
