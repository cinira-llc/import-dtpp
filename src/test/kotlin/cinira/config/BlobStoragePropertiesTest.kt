package cinira.config

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.WebApplicationType.NONE
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles

internal class BlobStoragePropertiesTest {

    @ParameterizedTest
    @ValueSource(strings = ["in-development,in-live-dev"])
    fun `Known configurations are loaded properly by Spring Boot`(profiles: String) {
        val allProfiles = arrayOf("wants-blob-storage") + profiles.split(',')
        SpringApplicationBuilder(Context::class.java)
            .profiles(*allProfiles)
            .properties(mapOf("service.blob-storage.9000" to "blob-storage:9000"))
            .web(NONE)
            .run()
    }

    @Configuration
    @EnableConfigurationProperties(BlobStorageProperties::class)
    open class Context(
        private val env: Environment
    ) {
        @Bean
        open fun validate(blob: BlobStorageProperties) = CommandLineRunner { _ ->
            if (env.acceptsProfiles(Profiles.of("in-live-dev"))) {
                assertThat(blob.backend).isEqualTo(BlobStorageProperties.BackendType.AMAZON_S3)
            } else {
                fail("Could not validate configuration for profiles: ${env.activeProfiles}")
            }
        }
    }
}
