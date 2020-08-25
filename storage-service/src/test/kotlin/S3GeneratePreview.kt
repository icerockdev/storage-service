/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import com.icerockdev.service.storage.preview.AbstractPreview
import com.icerockdev.service.storage.preview.JpegPreviewImpl
import com.icerockdev.service.storage.preview.PngPreviewImpl
import com.icerockdev.service.storage.preview.PreviewConfig
import com.icerockdev.service.storage.preview.PreviewService
import com.icerockdev.service.storage.preview.boundImage
import com.icerockdev.service.storage.preview.loadImage
import com.icerockdev.service.storage.s3.IS3Storage
import com.icerockdev.service.storage.s3.S3StorageImpl
import com.icerockdev.service.storage.s3.minioConfBuilder
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.io.File
import java.io.FileInputStream
import java.net.URI
import kotlin.test.assertFalse
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

        previewConfig = PreviewConfig().apply {
            append("jpg", JpegPreviewImpl(150, 150, 90, true).apply {
                prefix = "150x150/test_jpg"
            })
            append("png", PngPreviewImpl(150, 150, 9).apply {
                prefix = "150x150/test_png"
            })
        }

        previewService = PreviewService(storage = storage, srcBucket = bucketName) {
            previewPrefix = "preview"
        }
    }

    @Test
    fun testGetListDeleteAll() {
        // init storage
        if (!storage.bucketExist(bucketName)) {
            storage.createBucket(bucketName)
        }

        val fileName = storage.generateFileKey()

        val file = File(objectPath)
        val stream = FileInputStream(file)

        storage.put(bucketName, fileName, stream)

        runBlocking {
            previewService.generatePreview(fileName, previewConfig.getPreviewList(), AbstractPreview::boundImage)
        }

        for (preview in previewConfig.getPreviewList()) {
            assertTrue {
                storage.objectExists(bucketName, previewService.getPreviewName(fileName, preview))
            }
        }

        runBlocking {
            previewService.deletePreview(fileName, previewConfig.getPreviewList())
        }

        for (preview in previewConfig.getPreviewList()) {
            assertFalse {
                storage.objectExists(bucketName, previewService.getPreviewName(fileName, preview))
            }
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
        private lateinit var previewService: PreviewService
        private lateinit var previewConfig: PreviewConfig
    }
}