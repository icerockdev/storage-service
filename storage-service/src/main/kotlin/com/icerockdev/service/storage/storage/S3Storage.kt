/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.storage

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
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.ObjectCannedACL
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import software.amazon.awssdk.services.s3.model.S3Object
import java.io.InputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class S3Storage(private val client: S3Client) : Storage {
    override fun get(bucket: String, key: String): InputStream? {
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

    override fun isBucketExist(bucket: String): Boolean {
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

    override fun isObjectExists(bucket: String, key: String): Boolean {
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
        val request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .acl(ObjectCannedACL.PUBLIC_READ)
            .build()

        return try {
            client.putObject(request, RequestBody.fromBytes(stream.readAllBytes()))
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
        private val logger = LoggerFactory.getLogger(S3Storage::class.java)
    }
}
