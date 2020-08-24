/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.storage

import com.icerockdev.service.storage.dto.ObjectDto
import software.amazon.awssdk.services.s3.S3Client
import java.io.InputStream

class S3Storage(private val client: S3Client) : Storage {
    override fun get(bucket: String, key: String): ObjectDto {
        TODO("Not yet implemented")
    }

    override fun list(bucket: String, prefix: String): List<ObjectDto> {
        TODO("Not yet implemented")
    }

    override fun isBucketExist(bucket: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun isObjectExists(bucket: String, key: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun put(bucket: String, key: String, stream: InputStream): ObjectDto {
        TODO("Not yet implemented")
    }

    override fun copy(srcBucket: String, srcKey: String, dstBucket: String, dstKey: String): ObjectDto {
        TODO("Not yet implemented")
    }

    override fun delete(bucket: String, key: String): Boolean {
        TODO("Not yet implemented")
    }

}
