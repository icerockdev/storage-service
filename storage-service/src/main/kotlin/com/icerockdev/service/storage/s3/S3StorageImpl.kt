/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.s3

import org.slf4j.LoggerFactory
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
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
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.FilterInputStream
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URI
import java.net.URLConnection
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * TODO: implements S3AsyncClient and change to coroutine usage
 */
class S3StorageImpl(private val client: S3Client) : IS3Storage {
    override fun get(bucket: String, key: String): FilterInputStream? {
        return try {
            client.getObject(GetObjectRequest.builder()
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

    override fun share(endpoint: URI, bucket: String, key: String): String? {
        return try {
            client.utilities().getUrl(GetUrlRequest.builder()
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
            client.listObjectsV2(ListObjectsV2Request.builder()
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
            client.headObject(HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()
            )
            true
        } catch (e: NoSuchKeyException) {
            false
        }
    }

    override fun put(bucket: String, key: String, stream: InputStream): Boolean {
        return put(bucket, key, stream.buffered())
    }

    override fun put(bucket: String, key: String, byteArray: ByteArray): Boolean {
        val stream = byteArray.inputStream().buffered()
        return put(bucket, key, stream)
    }

    private fun put(bucket: String, key: String, stream: BufferedInputStream): Boolean {
        val contentType = URLConnection.guessContentTypeFromStream(stream)

        val request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .acl(ObjectCannedACL.PUBLIC_READ)
            .contentEncoding("UTF-8")
            .contentType(contentType)
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

    companion object {
        private val logger = LoggerFactory.getLogger(S3StorageImpl::class.java)
    }
}
