package com.icerockdev.service.storage.mime

import com.icerockdev.service.storage.mime.exception.MimeTypeNotDetectedException
import java.io.File
import java.io.InputStream
import org.apache.tika.Tika

object MimeTypeDetector {
    /**
     * For more info see https://tika.apache.org
     */
    private val tika = Tika()

    fun detect(stream: InputStream): MimeType {
        return tika.detect(stream)?.let {
            MimeType(baseType = it)
        } ?: throw MimeTypeNotDetectedException()
    }

    fun detect(file: File): MimeType {
        return tika.detect(file)?.let {
            MimeType(baseType = it)
        } ?: throw MimeTypeNotDetectedException()
    }
}
