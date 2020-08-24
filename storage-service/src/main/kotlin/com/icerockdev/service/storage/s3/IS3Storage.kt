/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.s3

import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.S3Object
import java.io.FilterInputStream
import java.io.InputStream
import java.util.UUID

// TODO: change return type for support file storage (if needed)
interface IS3Storage {
    fun get(bucket: String, key: String): FilterInputStream?

    fun list(bucket: String, prefix: String): List<S3Object>

    fun bucketExist(bucket: String): Boolean

    fun createBucket(bucket: String): Boolean

    fun deleteBucket(bucket: String): Boolean

    fun deleteBucketWithObjects(bucket: String): Boolean

    fun objectExists(bucket: String, key: String): Boolean

    fun put(bucket: String, key: String, stream: InputStream): Boolean

    fun put(bucket: String, key: String, byteArray: ByteArray): Boolean

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