/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.sample

import com.icerockdev.service.storage.s3.S3StorageImpl
import com.icerockdev.service.storage.s3.minioConfBuilder
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.ContentType
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI
import software.amazon.awssdk.services.s3.presigner.S3Presigner

object Main {
    private val dotenv = dotenv()
    private val s3 = S3Client.builder()
        .serviceConfiguration(minioConfBuilder)
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    dotenv["MINIO_ACCESS_KEY"], dotenv["MINIO_SECRET_KEY"]
                )
            )
        )
        .endpointOverride(URI.create(dotenv["S3_ENDPOINT"]!!))
        .region(Region.of(dotenv["S3_REGION"]))
        .build()

    private val preSigner = S3Presigner.builder()
        .serviceConfiguration(minioConfBuilder)
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    dotenv["MINIO_ACCESS_KEY"], dotenv["MINIO_SECRET_KEY"]
                )
            )
        )
        .endpointOverride(URI.create(dotenv["S3_ENDPOINT"]!!))
        .region(Region.of(dotenv["S3_REGION"]))
        .build()

    private val storage = S3StorageImpl(s3, preSigner)
    private val bucketName: String = dotenv["S3_BUCKET"]!!

    init {
        if (!storage.bucketExist(bucketName)) {
            storage.createBucket(bucketName)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val host = dotenv["APP_HOST"] ?: ""
        val port = dotenv["APP_PORT"]?.toInt() ?: 80

        val server = embeddedServer(Netty, host = host, port = port) {
            install(CallLogging)

            routing {
                post("/") {
                    upload(call)
                    call.respondText("Upload success!", ContentType.Text.Plain)
                }
            }
        }
        server.start(wait = true)

        Runtime.getRuntime().addShutdownHook(Thread(Runnable {
            server.stop(1000L, 3000L)
        }))
    }

    private suspend fun upload(call: ApplicationCall) {
        val multipart = call.receiveMultipart()
        multipart.forEachPart { part ->
            if (part is PartData.FileItem) {
                part.streamProvider().use { stream ->
                    storage.put(bucketName, part.originalFileName ?: "", stream)
                }
            }
            part.dispose()
        }
    }
}
