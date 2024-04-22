package cinira

import cinira.dtpp.MediaEntry
import cinira.dtpp.DatasetEntry
import cinira.dtpp.SegmentIndex
import cinira.dtpp.ThumbnailEntry
import cinira.util.put
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream
import org.apache.commons.io.FilenameUtils.getBaseName
import org.apache.commons.io.IOUtils.copyLarge
import org.apache.commons.io.input.CloseShieldInputStream
import org.apache.pdfbox.Loader
import org.apache.pdfbox.rendering.PDFRenderer
import org.slf4j.LoggerFactory.getLogger
import org.springframework.core.io.Resource
import org.springframework.util.MimeType
import org.springframework.util.MimeTypeUtils
import org.springframework.util.StreamUtils.drain
import software.amazon.awssdk.services.s3.S3Client
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URI
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.imageio.ImageIO
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension

/**
 * [ImportDtppSegment] defines the public interface to [ImportDtppSegmentImpl], and exists primarily for unit testing.
 */
interface ImportDtppSegment {
    fun execute(archive: Resource): URI
}

/**
 * [ImportDtppSegmentImpl] is the concrete implementation of the [ImportDtppSegment] interface.
 */
internal class ImportDtppSegmentImpl private constructor(
    private val client: S3Client,
    private val targetBucket: String,
    private val keywordParser: KeywordParser,
    private val metafileParser: MetafileParser
) : ImportDtppSegment {
    internal constructor(client: S3Client, targetBucket: String) : this(
        client = client,
        targetBucket = targetBucket,
        keywordParser = KeywordParser(),
        metafileParser = MetafileParser()
    )

    override fun execute(archive: Resource) =
        getBaseName(archive.filename).let { baseName ->
            val (segment, cycle) = baseName.split('_')
            log.debug("Extracting artifacts for segment [{}] from archive {}.", segment, archive)
            val index = archive.inputStream.use { input ->
                var acc = SegmentIndex(
                    cycle = cycle.toInt(),
                    segment = segment
                )
                ZipInputStream(input).let { zip ->
                    var entry = zip.nextEntry
                    while (null != entry) {
                        if (!entry.isDirectory) {
                            val path = entry.name
                            val name = path.substringAfterLast('/')
                            if ("d-TPP_Metafile.xml" == name) {
                                log.trace("Entry [$name] is the cycle metafile.")
                                acc = extractMetafile(acc, CloseShieldInputStream.wrap(zip))
                            } else if (!(name.endsWith(".PDF") && name.first().isDigit())) {
                                log.trace("Skipping entry [$path].")
                            } else if (path.startsWith("compare_pdf/")) {
                                log.trace("Entry [$path] is a chart diff.")
                                acc = extractDiff(acc, entry, CloseShieldInputStream.wrap(zip))
                            } else {
                                log.trace("Entry [$path] is a chart.")
                                acc = extractChart(acc, entry, CloseShieldInputStream.wrap(zip))
                            }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                    acc
                }
                val drained = drain(input)
                if (0 != drained) {
                    log.debug("Drained $drained remaining byte(s) from blob stream after import.")
                }
                acc
            }

            /* Store segment index. */
            produceIndex(index)
        }.uri

    private fun compressBzip2(data: ByteArray) =
        data.inputStream().use { input ->
            val output = ByteArrayOutputStream(data.size)
            BZip2CompressorOutputStream(output).use { compressor ->
                copyLarge(input, compressor)
            }
            output.toByteArray().also { compressed ->
                log.trace("Bzip2-compressed ${data.size}-byte input to ${compressed.size}-byte output.")
            }
        }

    private fun extractChart(acc: SegmentIndex, entry: ZipEntry, stream: InputStream) =
        stream.readAllBytes().let { buffer ->
            val name = entry.name
            log.debug("Processing chart [{}]", name)

            /* Store the chart PDF. */
            val size = buffer.size.toLong()
            val path = Path.of(name)
            val base = "content/library/ephemeral/faa-dtpp/${acc.cycle}"
            val key = "$base/pdf/$path"
            log.trace("Storing chart [{}] to [{}].", name, key)
            client.put(targetBucket, key, size, applicationPdf, buffer::inputStream)

            /* Generate and store the chart thumbnail(s). */
            val pdf = Loader.loadPDF(buffer)
            val renderer = PDFRenderer(pdf)
            val thumbnail60 = renderer.renderImageWithDPI(0, 60.0f)
            val png60 = ByteArrayOutputStream(256 * 1024).also { output ->
                ImageIO.write(thumbnail60, "png", output)
            }.toByteArray()
            val thumbnail60Key = "$base/img/${path.nameWithoutExtension}@60.png"
            log.trace("Storing chart [{}] 60dpi thumbnail to [{}].", name, thumbnail60Key)
            client.put(targetBucket, thumbnail60Key, png60.size.toLong(), MimeTypeUtils.IMAGE_PNG, png60::inputStream)

            /* Assemble and add the segment index entry. */
            val chart = MediaEntry(
                contentType = applicationPdf.toString(),
                name = name,
                size = size,
                type = MediaEntry.Type.CHART,
                thumbnail = listOf(
                    ThumbnailEntry(
                        contentType = MimeTypeUtils.IMAGE_PNG.toString(),
                        size = png60.size.toLong(),
                        dimensions = arrayOf(thumbnail60.width, thumbnail60.height),
                        dpi = 60
                    )
                )
            )
            acc.addMedia(keywordParser.parse(chart, pdf))
        }

    private fun extractDiff(acc: SegmentIndex, entry: ZipEntry, stream: InputStream) =
        stream.readAllBytes().let { buffer ->
            log.debug("Processing chart diff [{}]", entry.name)

            /* Store the diff PDF. */
            val size = buffer.size.toLong()
            val path = Path.of(entry.name)
            val name = path.name
            val base = "content/library/retained/faa-dtpp/${acc.cycle}"
            val key = "$base/pdf/$name"
            log.trace("Storing chart diff [{}] to [{}].", name, key)
            client.put(targetBucket, key, size, applicationPdf, buffer::inputStream)

            /* Generate and store the diff thumbnail(s). */
            val pdf = Loader.loadPDF(buffer)
            val renderer = PDFRenderer(pdf)
            val thumbnail60 = renderer.renderImageWithDPI(0, 60.0f)
            val png60 = ByteArrayOutputStream(256 * 1024).also { output ->
                ImageIO.write(thumbnail60, "png", output)
            }.toByteArray()
            val thumbnail60Key = "$base/img/${path.nameWithoutExtension}@60.png"
            log.trace("Storing chart revision [{}] 60dpi thumbnail to [{}].", name, thumbnail60Key)
            client.put(targetBucket, thumbnail60Key, png60.size.toLong(), MimeTypeUtils.IMAGE_PNG, png60::inputStream)

            /* Assemble and add the segment index entry. */
            val diff = MediaEntry(
                contentType = applicationPdf.toString(),
                name = name,
                size = size,
                type = MediaEntry.Type.DIFF,
                thumbnail = listOf(
                    ThumbnailEntry(
                        contentType = MimeTypeUtils.IMAGE_PNG.toString(),
                        size = png60.size.toLong(),
                        dimensions = arrayOf(thumbnail60.width, thumbnail60.height),
                        dpi = 60
                    )
                )
            )
            acc.addMedia(diff)
        }

    private fun extractMetafile(acc: SegmentIndex, stream: InputStream) =
        metafileParser.parse(acc, stream).let { metafile ->
            log.debug("Processing cycle metafile.")
            val bzip2JsonUtf8 = compressBzip2(json.writeValueAsBytes(metafile))
            val size = bzip2JsonUtf8.size.toLong()
            val key = "dataset/faa-dtpp/${acc.cycle}/metafile.json.bz2"
            log.trace("Storing cycle metafile to [{}].", key)
            val meta = client.put(targetBucket, key, size, applicationBzip2, bzip2JsonUtf8::inputStream)
            acc.addDataset(
                dataset = DatasetEntry(
                    name = meta.filename,
                    contentType = "application/json;charset=utf-8",
                    size = size,
                    type = DatasetEntry.Type.METAFILE,
                    wrapperContentType = applicationBzip2.toString()
                )
            )
        }

    private fun produceIndex(index: SegmentIndex) =
        json.writeValueAsBytes(index).let { jsonUtf8 ->
            log.debug("Generating segment index.")
            val bzip2JsonUtf8 = compressBzip2(jsonUtf8)
            val key = "dataset/faa-dtpp/${index.cycle}/index-${index.segment}.json.bz2"
            log.trace("Storing segment index to [{}].", key)
            client.put(targetBucket, key, bzip2JsonUtf8.size.toLong(), applicationBzip2, bzip2JsonUtf8::inputStream)
        }

    companion object {
        private val log = getLogger(ImportDtppSegmentImpl::class.java)
        private val applicationBzip2 = MimeType.valueOf("application/x-bzip2")
        private val applicationPdf = MimeType.valueOf("application/pdf")
        private val json = ObjectMapper()
            .registerModules(JavaTimeModule(), KotlinModule.Builder().build())
            .enable(SerializationFeature.INDENT_OUTPUT)
    }
}
