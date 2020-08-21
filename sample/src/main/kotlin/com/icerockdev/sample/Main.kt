/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.sample

import com.icerockdev.service.storage.config.AttachmentConfig
import com.icerockdev.service.storage.config.ImageConfig
import com.icerockdev.service.storage.config.PreviewConfig
import com.icerockdev.service.storage.config.StorageConfig
import com.icerockdev.service.storage.storage.S3Storage
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

object Main {
    private val dotenv = dotenv {
        directory = "./sample"
    }

    // Setup config
    private val imageConfig: ImageConfig = ImageConfig("com/icerockdev/service/storage/storage/img")
    private val previewConfig: PreviewConfig = PreviewConfig("com/icerockdev/service/storage/storage/preview")
    private val attachmentConfig: AttachmentConfig = AttachmentConfig("com/icerockdev/service/storage/storage/attachment")
    private val config: StorageConfig = StorageConfig(imageConfig, previewConfig, attachmentConfig)

    // Setup client
    private val minioClient: MinioClient = MinioClient.builder()
        .endpoint(dotenv["S3_ENDPOINT"])
        .credentials(dotenv["MINIO_ACCESS_KEY"], dotenv["MINIO_SECRET_KEY"])
        .region(dotenv["S3_REGION"])
        .build();

    // Setup storage
    private val storage: S3Storage = S3Storage(minioClient = minioClient, bucket = dotenv["S3_BUCKET"] ?: "")

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
                    storage.put(part.originalFileName ?: "", stream)
                }
            }
            part.dispose()
        }
    }
}
