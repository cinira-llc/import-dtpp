package cinira.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI

/**
 * [BlobStorageProperties] holds configuration related to blob storage.
 */
@ConfigurationProperties("cinira.component.blob-storage")
data class BlobStorageProperties(

    /**
     * Blob storage backend type.
     */
    var backend: BackendType,

    /**
     * Blob storage backend connection config. Can be `null` if configuration is provided in the environment and can be
     * bootstrapped by convention, such as in AWS Lambda with `DynamoDbAsyncClient.create()`.
     */
    var connection: ConnectionProperties?
) {
    /**
     * [BackendType] defines constants which correspond to the supported blob storage backends.
     */
    enum class BackendType {

        /**
         * Amazon S3, or something implementing the Amazon S3 API, such as MinIO.
         */
        AMAZON_S3
    }

    /**
     * [ConnectionProperties] holds connection and configuration properties for accessing the blob storage backend. This
     * is required whenever the blob storage cannot be bootstrapped by convention, but the precise combination of
     * required fields and the data they contain will vary by backend type.
     */
    data class ConnectionProperties(
        /**
         * Amazon S3 access key.
         */
        var accessKey: String? = null,

        /**
         * Amazon S3 secret key.
         */
        var secretKey: String? = null,

        /**
         * Azure Blob Storage connection string.
         */
        var connectionString: String? = null,

        /**
         * Storage provider endpoint URL.
         */
        var endpointUrl: URI? = null,

        /**
         * Storage region.
         */
        var region: String? = null
    )
}
