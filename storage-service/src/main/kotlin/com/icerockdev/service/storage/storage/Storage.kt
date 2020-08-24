/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.storage

import com.icerockdev.service.storage.dto.ObjectDto
import java.io.InputStream
import java.util.UUID

interface Storage {
    fun get(bucket: String, key: String): ObjectDto

    fun list(bucket: String, prefix: String): List<ObjectDto>

    fun isBucketExist(bucket: String): Boolean

    fun createBucket(bucket: String): Boolean

    fun isObjectExists(bucket: String, key: String): Boolean

    fun put(bucket: String, key: String, stream: InputStream): ObjectDto

    fun copy(srcBucket: String, srcKey: String, dstBucket: String, dstKey: String): ObjectDto

    fun delete(bucket: String, key: String): Boolean

    fun generateFileKey(): String {
        return UUID.randomUUID().toString().replace("-","/")
    }
}
