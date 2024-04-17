package cinira.util

import org.springframework.core.io.AbstractResource
import org.springframework.core.io.InputStreamSource
import org.springframework.util.MimeType
import software.amazon.awssdk.core.sync.RequestBody.fromInputStream
import software.amazon.awssdk.services.s3.S3Client
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.name

fun S3Client.get(bucket: String, key: String): S3ClientResource =
    headObject { get -> get.bucket(bucket).key(key) }.let { head ->
        val lastModified = head.lastModified().toEpochMilli()
        val type = MimeType.valueOf(head.contentType())
        val size = head.contentLength()
        S3ClientResource(
            bucket = bucket,
            key = key,
            type = type,
            size = size,
            lastModified = lastModified,
            source = { getObject { get -> get.bucket(bucket).key(key) } }
        )
    }

fun S3Client.put(bucket: String, key: String, size: Long, type: MimeType, source: InputStreamSource) =
    source.inputStream.use { input ->
        val lastModified = Instant.now().toEpochMilli()
        putObject({ put ->
            put.bucket(bucket).key(key).contentLength(size).contentType(type.toString())
        }, fromInputStream(input, size))
        S3ClientResource(
            bucket = bucket,
            key = key,
            type = type,
            size = size,
            lastModified = lastModified,
            source = { source.inputStream }
        )
    }

class S3ClientResource(
    val bucket: String,
    val key: String,
    val type: MimeType,
    private val size: Long,
    private val lastModified: Long,
    private val source: InputStreamSource
) : AbstractResource() {
    private val filename = Path.of(key).name
    private val uri = URI.create("s3://$bucket/$key")

    override fun contentLength() = size

    override fun getInputStream(): InputStream = source.inputStream

    override fun getDescription() = "S3ClientResource[bucket=$bucket, key=$key]"

    override fun getFilename() = filename

    override fun getURI(): URI = uri

    override fun getURL(): URL = uri.toURL()

    override fun lastModified() = lastModified
}
