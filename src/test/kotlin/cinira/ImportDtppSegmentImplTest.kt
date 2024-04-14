package cinira

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * [ImportDtppSegmentImplTest] provides unit test coverage for [ImportDtppSegmentImpl].
 *
 * Requires a known test segment (see [CHART_SEGMENT_RESOURCE] and [METAFILE_SEGMENT_RESOURCE]) to be available via the
 * `CINIRA_TEST_DATASET_ROOT` environment variable.
 */
@Disabled
@EnabledIfEnvironmentVariable(
    named = "CINIRA_TEST_DATASET_ROOT",
    matches = ".+",
    disabledReason = "Test datasets not found"
)
@ExtendWith(MockitoExtension::class)
@ExtendWith(SpringExtension::class)
internal class ImportDtppSegmentImplTest {

//    @Test
//    fun `import() correctly imports a known cycle successfully`(
//        @Value(SEGMENT_RESOURCES) segments: Array<Resource>,
//        @Mock(stubOnly = true) storage: BlobStorage
//    ) {
//        /* Configure mock BlobStorage to retrieve segment sources. */
////        assertThat(segments).hasSize(5)
//        val segmentsBySource = segments.associateBy { segment ->
//            "cinira-dev/source/faa/dtpp/${segment.filename}".toBlobPath()
//        }
//        whenever(storage.retrieve(isA())).then { invocation ->
//            val path: BlobPath = invocation.getArgument(0)
//            segmentsBySource[path]!!.toBlob(path) {
//                contentType = "application/x-zip-compressed"
//            }
//        }
//        whenever(storage.store(isA(), isA(), isA())).then { invocation ->
//            val path: BlobPath = invocation.getArgument(0)
//            val source: Supplier<InputStream> = invocation.getArgument(1)
//            val configurer: BlobAttributesConfigurer = invocation.getArgument(2)
//            val extensions = path.extensions.map(String::lowercase)
//            if (listOf("json", "bz2") == extensions) {
//
//                /* Extracted JSON is a metafile or segment index. */
//                val baseName = path.baseName
//                if ("metafile-220714.json" == baseName) {
//                    val metafile = source.get().let(::BZip2CompressorInputStream).use { stream ->
//                        json.readValue(stream.bufferedReader(UTF_8), CycleMetafile::class.java)
//                    }
//                    assertThat(metafile.cycle).isEqualTo(2207)
//                    assertThat(metafile.effectiveStartDateTime).isEqualTo("2022-07-14T09:01:00Z")
//                    assertThat(metafile.effectiveEndDateTime).isEqualTo("2022-08-11T09:01:00Z")
//                } else if (baseName.startsWith("segment-220714-")) {
//                    val segment = baseName.substring(15..20)
//                    val index = source.get().let(::BZip2CompressorInputStream).use { stream ->
//                        json.readValue(stream.bufferedReader(UTF_8), SegmentIndex::class.java)
//                    }
//                    assertThat(index.segment).isEqualTo(segment)
//                    when (segment) {
//                        "DDTPPA" -> {
//                            assertThat(index.charts).hasSize(4203)
//                            assertThat(index.items).hasSize(4203)
//                        }
//
//                        "DDTPPB" -> {
//                            assertThat(index.charts).hasSize(4396)
//                            assertThat(index.items).hasSize(4396)
//                        }
//
//                        "DDTPPC" -> {
//                            assertThat(index.charts).hasSize(4076)
//                            assertThat(index.items).hasSize(4076)
//                        }
//
//                        "DDTPPD" -> {
//                            assertThat(index.charts).hasSize(4105)
//                            assertThat(index.items).hasSize(4105)
//                        }
//
//                        "DDTPPE" -> {
//                            assertThat(index.charts).isEmpty()
//                            assertThat(index.items).hasSize(1581)
//                        }
//
//                        else -> fail("Unexpected index for segment [$segment].")
//                    }
//                } else {
//                    fail("Unexpected artifact stored at path [$path]")
//                }
//            } else if (listOf("pdf") == extensions) {
//
//                /* Extracted PDFs are always charts or chart diffs. Check size, content type, and magic number. */
//                val data = source.get().use(InputStream::readAllBytes)
//                assertThat(String(data.slice(0..3).toByteArray(), UTF_8)).isEqualTo(pdfMagic)
//                val attributes = Blob.Attributes.build(configurer)
//                assertThat(attributes.contentType).isEqualTo("application/pdf")
//                assertThat(attributes.size).isEqualTo(data.size.toLong())
//            } else {
//                assertThat(true).isFalse
//            }
//        }
//
//        val instance = ImportDtppSegmentImpl(
//            storage = storage,
//            mapper = BlobPathMapper.forAntPaths(
//                config = BlobPathMapperConfig.fromMap(
//                    map = environment,
//                    prefixes = arrayOf("CHART_", "DIFF_", "METAFILE_", "SEGMENT_")
//                )
//            )
//        )
//        segments.forEach { segment ->
//            val source = "cinira-dev/source/faa/dtpp/${segment.filename}".toBlobPath()
//            instance.execute(source)
//        }
//    }
//
//    companion object {
//        private const val SEGMENT_RESOURCES = "file:///\${CINIRA_TEST_DATASET_ROOT}/faa-dtpp/DDTPPE_220714.zip"
//        private const val pdfMagic = "%PDF"
//        private val environment = mapOf(
//            "CHART_SOURCE_PATTERN" to "{bucket}/source/faa/dtpp/{segment}_{cycle}.zip/{name}.PDF",
//            "CHART_SOURCE_EXCLUDE" to "^README_",
//            "CHART_TARGET_FORMAT_CHART" to "{bucket}/media/faa/dtpp/chart/{cycle}/{name}.PDF",
//            "DIFF_SOURCE_PATTERN" to "{bucket}/source/faa/dtpp/{segment}_{cycle}.zip/compare_pdf/{name}_CMP.PDF",
//            "DIFF_TARGET_FORMAT_DIFF" to "{bucket}/media/faa/dtpp/diff/{cycle}/{name}.PDF",
//            "METAFILE_SOURCE_PATTERN" to "{bucket}/source/faa/dtpp/{segment}_{cycle}.zip/d-TPP_Metafile.xml",
//            "METAFILE_TARGET_FORMAT_METAFILE" to "{bucket}/dataset/faa/dtpp/metafile-{cycle}.json.bz2",
//            "SEGMENT_SOURCE_PATTERN" to "{bucket}/source/faa/dtpp/{segment}_{cycle}.zip",
//            "SEGMENT_TARGET_FORMAT_INDEX" to "{bucket}/meta/faa/dtpp/segment-{cycle}-{segment}.json.bz2"
//        )
//        private val json = ObjectMapper().registerModules(JavaTimeModule(), KotlinModule.Builder().build())
//    }
}
