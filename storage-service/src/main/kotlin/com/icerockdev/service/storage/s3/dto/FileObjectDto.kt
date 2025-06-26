package com.icerockdev.service.storage.s3.dto

import java.io.BufferedInputStream

data class FileObjectDto(
    val inputStream: BufferedInputStream,
    val size: Long,
    val contentType: String? = null,
)
