package cinira

import cinira.config.ImportDtppProperties
import cinira.model.ItemDetails
import cinira.model.SegmentIndex
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream
import org.apache.commons.io.FilenameUtils.getBaseName
import org.apache.commons.io.IOUtils.copyLarge
import org.apache.commons.io.input.CloseShieldInputStream
import org.slf4j.LoggerFactory.getLogger
import org.springframework.core.io.WritableResource
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.util.StreamUtils.drain
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URI
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.io.path.nameWithoutExtension

/**
 * [ImportDtppSegment] defines the public interface to [ImportDtppSegmentImpl], and exists primarily for unit testing.
 */
interface ImportDtppSegment {

    /**
     * Import a DTPP segment archive.
     *
     * @param uri the source `s3:` URI.
     * @return [URI] the `s3:` URI to which the segment index was written.
     */
    fun execute(uri: URI): URI

    companion object {

        /**
         * Create a [ImportDtppSegment] instance.
         *
         * @param resolver the [ResourcePatternResolver] component.
         * @param config the [ImportDtppProperties] configuration.
         * @return [ImportDtppSegment] instance.
         */
        fun create(resolver: ResourcePatternResolver, config: ImportDtppProperties): ImportDtppSegment =
            ImportDtppSegmentImpl(resolver, config)
    }
}

/**
 * [ImportDtppSegmentImpl] is the concrete implementation of the [ImportDtppSegment] interface.
 */
internal class ImportDtppSegmentImpl(
    private val resolver: ResourcePatternResolver,
    private val chartParser: ChartParser,
    private val metafileParser: MetafileParser,
    private val config: ImportDtppProperties
) : ImportDtppSegment {
    constructor(
        resolver: ResourcePatternResolver,
        config: ImportDtppProperties
    ) : this(
        resolver = resolver,
        chartParser = ChartParser(),
        metafileParser = MetafileParser(),
        config = config
    )

    override fun execute(uri: URI) =
        getBaseName(uri.path).let { baseName ->
            val (segment, cycle) = baseName.split('_')
            val archive = resolver.getResource(uri.toString());
            log.debug("Extracting artifacts from segment [{}] from archive at [{}].", segment, uri)
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
                                acc = extractMetafile(acc, uri, CloseShieldInputStream.wrap(zip))
                            } else if (!(name.endsWith(".PDF") && name.first().isDigit())) {
                                log.trace("Skipping entry [$path].")
                            } else if (path.startsWith("compare_pdf/")) {
                                log.trace("Entry [$path] is a chart diff.")
                                acc = extractDiff(acc, uri, entry, CloseShieldInputStream.wrap(zip))
                            } else {
                                log.trace("Entry [$path] is a chart.")
                                acc = extractChart(acc, uri, entry, CloseShieldInputStream.wrap(zip))
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
            produceIndex(uri, index)
        }

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

    private fun extractChart(acc: SegmentIndex, segment: URI, entry: ZipEntry, stream: InputStream) =
        stream.readBytes().let { buffer ->
            val bufferSize = buffer.size.toLong()
            val target = config.incoming.resolve(
                segment, config.destinations.charts,
                mapOf("baseName" to Path.of(entry.name).nameWithoutExtension)
            )
            log.debug("Storing chart [{}] to [{}].", entry.name, target)
            (resolver.getResource(target.toString()) as WritableResource)
                .outputStream.use { output ->
                    buffer.inputStream().use { input ->
                        copyLarge(input, output)
                    }
                }
            buffer.inputStream().use { input ->
                acc.addChart(
                    chart = chartParser.parse(entry.name, input),
                    item = ItemDetails(
                        uri = target,
                        contentType = "application/pdf",
                        size = bufferSize,
                        type = ItemDetails.ItemType.CHART
                    )
                )
            }
        }

    private fun extractDiff(acc: SegmentIndex, segment: URI, entry: ZipEntry, stream: InputStream) =
        entry.size.let { entrySize ->

            /* Note: wrapping in InputStreamResource so an exception would be thrown if read more than once. */
            val target = config.incoming.resolve(
                segment,
                config.destinations.diffs,
                mapOf("baseName" to Path.of(entry.name).nameWithoutExtension)
            )
            log.debug("Storing chart diff [{}] to [{}].", entry.name, target)
            (resolver.getResource(target.toString()) as WritableResource)
                .outputStream.use { output ->
                    copyLarge(stream, output)
                }
            acc.addItem(
                item = ItemDetails(
                    uri = target,
                    contentType = "application/pdf",
                    size = entrySize,
                    type = ItemDetails.ItemType.DIFF
                )
            )
        }


    private fun extractMetafile(acc: SegmentIndex, segment: URI, stream: InputStream) =
        metafileParser.parse(stream).let { metafile ->
            val bzip2JsonUtf8 = compressBzip2(json.writeValueAsBytes(metafile))
            val target = config.incoming.resolve(segment, config.destinations.metafile)
            log.debug("Storing DTPP segment metafile to [{}].", target)
            (resolver.getResource(target.toString()) as WritableResource)
                .outputStream.use { output ->
                    bzip2JsonUtf8.inputStream().use { input ->
                        copyLarge(input, output)
                    }
                }
            acc.addItem(
                item = ItemDetails(
                    uri = target,
                    contentType = "application/json;charset=utf-8",
                    size = bzip2JsonUtf8.size.toLong(),
                    type = ItemDetails.ItemType.METAFILE,
                    wrapperContentType = "application/x-bzip2"
                )
            )
        }

    private fun produceIndex(path: URI, index: SegmentIndex) =
        json.writeValueAsBytes(index).let { jsonUtf8 ->
            val bzip2JsonUtf8 = compressBzip2(jsonUtf8)
            val target = config.incoming.resolve(path, config.destinations.segments)
            log.debug("Storing DTPP cycle index to [{}].", target)
            (resolver.getResource(target.toString()) as WritableResource)
                .outputStream.use { output ->
                    bzip2JsonUtf8.inputStream().use { input ->
                        copyLarge(input, output)
                    }
                }
            target
        }

    companion object {
        private val log = getLogger(ImportDtppSegmentImpl::class.java)
        private val json = ObjectMapper()
            .registerModules(JavaTimeModule(), KotlinModule.Builder().build())
            .enable(SerializationFeature.INDENT_OUTPUT)
    }
}
