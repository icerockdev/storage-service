/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.sample

import com.icerockdev.service.storage.config.AttachmentConfig
import com.icerockdev.service.storage.config.ImageConfig
import com.icerockdev.service.storage.config.PreviewConfig
import com.icerockdev.service.storage.config.StorageConfig
import com.icerockdev.service.storage.storage.MinIOStorage
import io.github.cdimascio.dotenv.dotenv
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.request.receiveMultipart
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.minio.MinioClient
import java.net.URI
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Configuration

object Main {
    private val dotenv = dotenv {
        directory = "./sample"
    }

    // Setup config
    private val imageConfig: ImageConfig = ImageConfig("com/icerockdev/service/storage/storage/img")
    private val previewConfig: PreviewConfig = PreviewConfig("com/icerockdev/service/storage/storage/preview")
    private val attachmentConfig: AttachmentConfig =
        AttachmentConfig("com/icerockdev/service/storage/storage/attachment")
    private val config: StorageConfig = StorageConfig(imageConfig, previewConfig, attachmentConfig)

    // Setup client
    private val minIOClient: MinioClient = MinioClient.builder()
        .endpoint(dotenv["S3_ENDPOINT"])
        .credentials(dotenv["MINIO_ACCESS_KEY"], dotenv["MINIO_SECRET_KEY"])
        .region(dotenv["S3_REGION"])
        .build()

    // Setup storage
    private val minIOStorage: MinIOStorage = MinIOStorage(minIOClient = minIOClient, bucket = dotenv["S3_BUCKET"] ?: "")

    // Setup S3 client
    private val s3Client = S3AsyncClient.builder()
        .region(Region.of(dotenv["S3_REGION"]))
        .endpointOverride(URI.create(dotenv["S3_ENDPOINT"] ?: ""))
        .serviceConfiguration(
            S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .checksumValidationEnabled(false)
                .build()
        )
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(dotenv["MINIO_ACCESS_KEY"], dotenv["MINIO_SECRET_KEY"])
            )
        )


    @JvmStatic
    fun main(args: Array<String>) {
        val host = dotenv["APP_HOST"] ?: ""
        val port = dotenv["APP_PORT"]?.toInt() ?: 80

        val server = embeddedServer(Netty, host = host, port = port) {
            routing {
                post("/") {
                    upload(call)
                    call.respondText("Upload success!", ContentType.Text.Plain)
                }
            }
        }
        server.start(wait = true)
    }

    private suspend fun upload(call: ApplicationCall) {
        val multipart = call.receiveMultipart()
        multipart.forEachPart { part ->
            if (part is PartData.FileItem) {
                part.streamProvider().use { stream ->
                    minIOStorage.put(part.originalFileName ?: "", stream)
                }
            }
            part.dispose()
        }
    }
}
