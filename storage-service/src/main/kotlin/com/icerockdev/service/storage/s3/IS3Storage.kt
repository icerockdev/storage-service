/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.s3

import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.S3Object
import java.io.InputStream
import java.net.URI
import java.time.Duration
import java.util.UUID

// TODO: change return type for support file storage (if needed)
interface IS3Storage {
    fun get(bucket: String, key: String): ResponseInputStream<GetObjectResponse>?

    fun getBytes(bucket: String, key: String): ByteArray?

    fun getUrl(endpoint: URI, bucket: String, key: String): String?

    fun share(bucket: String, key: String, duration: Duration): String?

    fun list(bucket: String, prefix: String): List<S3Object>

    fun bucketExist(bucket: String): Boolean

    fun createBucket(bucket: String): Boolean

    fun deleteBucket(bucket: String): Boolean

    fun deleteBucketWithObjects(bucket: String): Boolean

    fun objectExists(bucket: String, key: String): Boolean

    fun put(bucket: String, key: String, stream: InputStream, metadata: Map<String, String>? = null): Boolean

    fun put(bucket: String, key: String, byteArray: ByteArray, metadata: Map<String, String>? = null): Boolean

    fun copy(srcBucket: String, srcKey: String, dstBucket: String, dstKey: String): Boolean

    /**
     * @return true, if correct execution (for does not exists object too), false otherwise
     */
    fun delete(bucket: String, key: String): Boolean

    fun generateFileKey(): String {
        return UUID.randomUUID().toString().replace("-","/")
    }
}

val minioConfBuilder: S3Configuration =
    S3Configuration
        .builder()
        .pathStyleAccessEnabled(true)
        .checksumValidationEnabled(false)
        .build()
