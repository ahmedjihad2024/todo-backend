package org.personal.todo.common.storage.backend

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.net.URI

/**
 * S3-compatible [FileStorage], active when `app.storage.backend` is `s3`.
 *
 * The same client speaks to Cloudflare R2, MinIO, and AWS S3 — only config differs:
 *  - AWS S3 : leave `endpoint` blank, set a real `region` (e.g. eu-central-1).
 *  - R2     : set `endpoint` to https://<account>.r2.cloudflarestorage.com, region `auto`.
 *  - MinIO  : set `endpoint` to your MinIO URL, any region, path-style on.
 *
 * Path-style access is on by default because R2/MinIO need it; AWS tolerates it too.
 */
@Component
@ConditionalOnProperty(name = ["app.storage.backend"], havingValue = "s3")
class S3FileStorage(
    @Value($$"${app.storage.s3.endpoint:}") private val endpoint: String,
    @Value($$"${app.storage.s3.region:auto}") private val region: String,
    @Value($$"${app.storage.s3.bucket}") private val bucket: String,
    @Value($$"${app.storage.s3.access-key}") private val accessKey: String,
    @Value($$"${app.storage.s3.secret-key}") private val secretKey: String,
    @Value($$"${app.storage.s3.path-style:true}") private val pathStyle: Boolean,
) : FileStorage {

    private lateinit var client: S3Client

    @PostConstruct
    fun init() {
        val builder = S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(
                StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)),
            )
            .forcePathStyle(pathStyle)
        if (endpoint.isNotBlank()) {
            builder.endpointOverride(URI.create(endpoint))
        }
        client = builder.build()
    }

    @PreDestroy
    fun close() {
        client.close()
    }

    override fun put(key: String, bytes: ByteArray, contentType: String) {
        client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build(),
            RequestBody.fromBytes(bytes),
        )
    }

    override fun get(key: String): ByteArray? =
        try {
            client.getObjectAsBytes(
                GetObjectRequest.builder().bucket(bucket).key(key).build(),
            ).asByteArray()
        } catch (_: NoSuchKeyException) {
            null
        }

    override fun delete(key: String) {
        client.deleteObject(
            DeleteObjectRequest.builder().bucket(bucket).key(key).build(),
        )
    }
}
