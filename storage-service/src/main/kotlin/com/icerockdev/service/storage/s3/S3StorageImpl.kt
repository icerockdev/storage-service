/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.s3

import com.icerockdev.service.storage.s3.policy.builder.PolicyBuilder
import com.icerockdev.service.storage.s3.policy.builder.PrincipalBuilder
import com.icerockdev.service.storage.s3.policy.builder.StatementBuilder
import com.icerockdev.service.storage.s3.policy.dto.Principal
import com.icerockdev.service.storage.s3.policy.dto.Statement
import org.slf4j.LoggerFactory
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import com.icerockdev.service.storage.mime.MimeTypeDetector
import com.icerockdev.service.storage.s3.policy.builder.ResourceBuilder
import com.icerockdev.service.storage.s3.policy.dto.Resource
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Duration
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.GetUrlRequest
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import software.amazon.awssdk.services.s3.model.S3Object

/**
 * TODO: implements S3AsyncClient and change to coroutine usage
 */
class S3StorageImpl(private val client: S3Client, private val preSigner: S3Presigner) : IS3Storage {

    override fun get(bucket: String, key: String): ResponseInputStream<GetObjectResponse>? {
        return try {
            client.getObject(
                GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build()
            )
        } catch (e: NoSuchKeyException) {
            null
        }
    }

    override fun getBytes(bucket: String, key: String): ByteArray? {
        val stream = get(bucket, key) ?: return null
        return stream.use { it.readBytes() }
    }

    /**
     * Get pre-signed URL with TTL
     */
    override fun share(bucket: String, key: String, duration: Duration): String? {
        return try {
            val getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()
            preSigner.presignGetObject(
                GetObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .getObjectRequest(getObjectRequest)
                    .build()
            ).url().toExternalForm()
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * Get permanent URL (public access bucket)
     */
    override fun getUrl(endpoint: URI, bucket: String, key: String): String? {
        return try {
            client.utilities().getUrl(
                GetUrlRequest.builder()
                    .endpoint(endpoint)
                    .bucket(bucket)
                    .key(key)
                    .build()
            ).toExternalForm()
        } catch (e: MalformedURLException) {
            null
        }
    }

    override fun list(bucket: String, prefix: String): List<S3Object> {
        return try {
            client.listObjectsV2(
                ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .build()
            ).contents()
        } catch (e: S3Exception) {
            logger.error(e.localizedMessage, e)
            emptyList()
        }
    }

    override fun bucketExist(bucket: String): Boolean {
        return try {
            client.headBucket(HeadBucketRequest.builder().bucket(bucket).build())
            true
        } catch (e: NoSuchBucketException) {
            false
        }
    }

    override fun createBucket(bucket: String): Boolean {
        return try {
            client.createBucket(
                CreateBucketRequest
                    .builder()
                    .bucket(bucket)
                    .build()
            )
            true
        } catch (e: BucketAlreadyExistsException) {
            false
        } catch (e: BucketAlreadyOwnedByYouException) {
            false
        } catch (e: S3Exception) {
            logger.error(e.localizedMessage, e)
            false
        }
    }

    override fun deleteBucket(bucket: String): Boolean {
        return try {
            client.deleteBucket(DeleteBucketRequest.builder().bucket(bucket).build())
            true
        } catch (e: NoSuchBucketException) {
            false
        }
    }

    override fun deleteBucketWithObjects(bucket: String): Boolean {
        list(bucket, "").forEach {
            delete(bucket, it.key())
        }
        return deleteBucket(bucket)
    }

    override fun objectExists(bucket: String, key: String): Boolean {
        return try {
            client.headObject(
                HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build()
            )
            true
        } catch (e: NoSuchKeyException) {
            false
        }
    }

    override fun put(bucket: String, key: String, stream: InputStream, metadata: Map<String, String>?): Boolean {
        return put(bucket, key, stream.buffered(), metadata)
    }

    override fun put(bucket: String, key: String, byteArray: ByteArray, metadata: Map<String, String>?): Boolean {
        val stream = byteArray.inputStream().buffered()
        return put(bucket, key, stream, metadata)
    }

    private fun put(bucket: String, key: String, stream: BufferedInputStream, metadata: Map<String, String>?): Boolean {

        val request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .acl(ObjectCannedACL.PUBLIC_READ)
            .contentEncoding("UTF-8")
            .contentType(MimeTypeDetector.detect(stream).toString())
            .metadata(metadata ?: emptyMap())
            .build()

        return try {
            client.putObject(request, RequestBody.fromBytes(stream.readBytes()))
            true
        } catch (e: S3Exception) {
            false
        }
    }

    override fun copy(srcBucket: String, srcKey: String, dstBucket: String, dstKey: String): Boolean {

        return try {
            val encodedUrl = URLEncoder.encode("$srcBucket/$srcKey", StandardCharsets.UTF_8.toString())
            val request = CopyObjectRequest.builder()
                .copySource(encodedUrl)
                .destinationBucket(dstBucket)
                .destinationKey(dstKey)
                .build()

            client.copyObject(request)
            true
        } catch (e: NoSuchBucketException) {
            false
        } catch (e: NoSuchKeyException) {
            false
        }
    }

    override fun delete(bucket: String, key: String): Boolean {
        return try {
            val response = client.deleteObject(
                DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build()
            )
            response.sdkHttpResponse().isSuccessful
        } catch (e: S3Exception) {
            logger.error(e.localizedMessage, e)
            false
        }
    }

    override fun getBucketPolicy(bucket: String): String? {
        return try {
            client.getBucketPolicy(
                GetBucketPolicyRequest.builder()
                    .bucket(bucket)
                    .build()
            ).policy()
        } catch (e: S3Exception) {
            if (e.statusCode() != 404) {
                logger.error(e.localizedMessage, e)
            }
            null
        }
    }

    override fun putBucketPolicy(
        bucket: String,
        confirmRemoveSelfBucketAccess: Boolean,
        configure: PolicyBuilder.() -> Unit
    ): Boolean {
        return try {
            val response = client.putBucketPolicy(
                PutBucketPolicyRequest.builder()
                    .bucket(bucket)
                    .confirmRemoveSelfBucketAccess(confirmRemoveSelfBucketAccess)
                    .policy(PolicyBuilder().apply(configure).build())
                    .build()
            )
            response.sdkHttpResponse().isSuccessful
        } catch (e: S3Exception) {
            logger.error(e.localizedMessage, e)
            false
        } catch (e: Exception) {
            logger.error(e.localizedMessage, e)
            false
        }
    }

    override fun deleteBucketPolicy(bucket: String): Boolean {
        return try {
            val response = client.deleteBucketPolicy(
                DeleteBucketPolicyRequest.builder()
                    .bucket(bucket)
                    .build()
            )
            response.sdkHttpResponse().isSuccessful
        } catch (e: S3Exception) {
            logger.error(e.localizedMessage, e)
            false
        }
    }

    override fun buildStatement(configure: StatementBuilder.() -> Unit): Statement =
        StatementBuilder().apply(configure).build()

    override fun buildPrincipal(configure: PrincipalBuilder.() -> Unit): Principal =
        PrincipalBuilder().apply(configure).build()

    override fun buildResource(configure: ResourceBuilder.() -> Unit): String =
        ResourceBuilder().apply(configure).build()

    companion object {
        private val logger = LoggerFactory.getLogger(S3StorageImpl::class.java)
    }
}
