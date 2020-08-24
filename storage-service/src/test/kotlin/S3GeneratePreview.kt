/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import com.icerockdev.service.storage.preview.generateAndStorePreview
import com.icerockdev.service.storage.s3.S3StorageImpl
import com.icerockdev.service.storage.s3.IS3Storage
import com.icerockdev.service.storage.s3.minioConfBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import kotlin.math.min
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class S3GeneratePreview {

    private val bucketName = "test"
    private val objectPath = "/home/alexsh/2.jpg"

    @Before
    fun init() {
        // TODO: load credentials from env
        s3 = S3Client.builder()
            .serviceConfiguration(minioConfBuilder)
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        "my_access_key", "my_secret_key"
                    )
                )
            )
            .endpointOverride(URI.create("http://127.0.0.30:9000"))
            .region(region)
            .build()

        storage = S3StorageImpl(s3)
    }

    @Test
    fun testGetListDeleteAll() {
        // init storage
        if (!storage.bucketExist(bucketName)) {
            storage.createBucket(bucketName)
        }

        val fileName = storage.generateFileKey()
        val previewName = "preview/$fileName"

        val file = File(objectPath)
        val stream = FileInputStream(file)

        storage.put(bucketName, fileName, stream)

        storage.generateAndStorePreview(bucketName, fileName, bucketName, previewName)

        assertTrue {
            storage.objectExists(bucketName, previewName)
        }

        storage.deleteBucketWithObjects(bucketName)
    }

    @After
    fun close() {
        s3.close()
    }

    companion object {
        private val region = Region.US_WEST_2

        private lateinit var s3: S3Client
        private lateinit var storage: IS3Storage
    }
}