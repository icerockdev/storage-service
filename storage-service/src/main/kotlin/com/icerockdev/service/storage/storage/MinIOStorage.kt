/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.storage

import com.icerockdev.service.storage.dto.ObjectDto
import com.icerockdev.service.storage.dto.S3ObjectDto
import io.minio.BucketExistsArgs
import io.minio.GetObjectArgs
import io.minio.ListObjectsArgs
import io.minio.MinioClient
import io.minio.ObjectWriteResponse
import io.minio.PutObjectArgs
import io.minio.StatObjectArgs
import io.minio.messages.Item
import java.io.InputStream

class MinIOStorage(private val minIOClient: MinioClient, private val bucket: String) : Storage {
    override fun get(key: String): ObjectDto {
        return try {
            val stream: InputStream = minIOClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(key)
                    .build()
            )

            S3ObjectDto(stream = stream)
        } catch (exception: Exception) {
            throw StorageException(message = exception.message)
        }
    }

    override fun list(prefix: String): List<ObjectDto> {
        return try {
            val resultIterator = minIOClient.listObjects(ListObjectsArgs.builder().bucket(bucket).build())
            val s3ObjectDtoList: MutableList<S3ObjectDto> = mutableListOf()
            resultIterator.forEach { result ->
                run {
                    val item: Item = result.get()
                    s3ObjectDtoList.add(S3ObjectDto(bucket = bucket, size = item.size()))
                }
            }
            s3ObjectDtoList
        } catch (exception: Exception) {
            throw StorageException(message = exception.message)
        }
    }

    override fun isTargetExist(key: String): Boolean {
        return try {
            minIOClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
        } catch (exception: Exception) {
            throw StorageException(message = exception.message)
        }
    }

    override fun isObjectExists(key: String): Boolean {
        return try {
            minIOClient.statObject(StatObjectArgs.builder().bucket(bucket).`object`(key).build()) != null
        } catch (exception: Exception) {
            throw StorageException(message = exception.message)
        }
    }

    override fun put(key: String, stream: InputStream): ObjectDto {
        return try {
            val response: ObjectWriteResponse = minIOClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .`object`(key)
                .stream(stream, -1, 10485760)
                .contentType("image/png")
                .build())

            S3ObjectDto()
        } catch (exception: Exception) {
            throw StorageException(message = exception.message)
        }
    }

    override fun copy(key: String, stream: InputStream): ObjectDto {
        TODO("Not yet implemented")
    }

    override fun delete(key: String): Boolean {
        TODO("Not yet implemented")
    }

}
