package cinira

import org.apache.commons.codec.binary.Hex
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.S3Object
import java.security.MessageDigest
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask

/**
 * [ImportDtppSegmentImplTest] provides unit test coverage for [ImportDtppSegmentImpl].
 */
@EnabledIfEnvironmentVariable(
    named = "CINIRA_TEST_DATASET_ROOT",
    matches = ".+",
    disabledReason = "Test datasets not found"
)
@Testcontainers
@ExtendWith(SpringExtension::class)
internal class ImportDtppSegmentImplTest {

    @Container
    private val localstack = LocalStackContainer(DockerImageName.parse("localstack/localstack"))
        .withServices(LocalStackContainer.Service.S3)

    @Test
    fun `import() correctly imports a known cycle successfully`(
        @Autowired resolver: ResourcePatternResolver,
        @Value("file:///\${CINIRA_TEST_DATASET_ROOT}/faa-dtpp/DDTPP*.zip") segments: Array<Resource>
    ) {
        assertThat(segments).isNotEmpty
        val credentials = AwsBasicCredentials.create(localstack.accessKey, localstack.secretKey)
        val client = S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .endpointOverride(localstack.endpoint)
            .forcePathStyle(true)
            .region(Region.of("us-east-1"))
            .build()
        client.createBucket { create -> create.bucket("cinira") }
        val instance = ImportDtppSegmentImpl(client, KeywordParser(), MetafileParser(), "cinira")
        val tasks = segments.map { segment ->
            ForkJoinPool.commonPool().submit {
                instance.execute(segment)
            }
        }
        tasks.forEach(ForkJoinTask<*>::join)
        val digest = MessageDigest.getInstance("SHA-256")
        client.listObjects { list -> list.bucket("cinira") }
            .contents()
            .sortedBy(S3Object::key)
            .map { obj -> digest.update("${obj.eTag()}:${obj.key()}".toByteArray()) }
        assertThat(Hex.encodeHexString(digest.digest()))
            .isEqualTo("f7d59527608cabb96abf8f8d6f42f6197746bad4a93dcf62d1cb665ee38c4e65")
    }
}
