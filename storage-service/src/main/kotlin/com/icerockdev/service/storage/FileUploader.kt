///*
// * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
// */
//
//package com.icerockdev.service.storage
//
//import com.icerockdev.service.storage.config.StorageConfig
//import com.icerockdev.service.storage.dto.ObjectDto
//import com.icerockdev.service.storage.s3.IS3Storage
//import io.ktor.application.ApplicationCall
//import io.ktor.request.receiveMultipart
//
//class FileUploader(private val IS3Storage: IS3Storage, private val bucket: String, private val config: StorageConfig) {
//
//    suspend fun uploadImageMultipart(call: ApplicationCall, partName: String) {
//        uploadImagesMultipart(call, listOf(partName))
//    }
//
//    suspend fun uploadImagesMultipart(call: ApplicationCall, partNames: List<String>) {
//        val objectDtoList = uploadMultipart(call, partNames)
//    }
//
//    suspend fun uploadAttachmentMultipart(call: ApplicationCall, partName: String) {
//        uploadAttachmentsMultipart(call, listOf(partName))
//    }
//
//    suspend fun uploadAttachmentsMultipart(call: ApplicationCall, partNames: List<String>) {
//        val objectDtoList = uploadMultipart(call, partNames)
//    }
//
//
//    suspend fun uploadMultipart(call: ApplicationCall, partNames: List<String>): List<ObjectDto> {
//        val multipart = call.receiveMultipart()
//        val objectDtoList: MutableList<ObjectDto> = mutableListOf()
////        multipart.forEachPart { part ->
////            if (part is PartData.FileItem && part.name in partNames) {
////                part.streamProvider().use { stream ->
////                    val objectDto = storage.put(bucket, part.originalFileName ?: "", stream)
////                    objectDtoList.add(objectDto)
////                }
////            }
////            part.dispose()
////        }
//
//        return objectDtoList
//    }
//}
