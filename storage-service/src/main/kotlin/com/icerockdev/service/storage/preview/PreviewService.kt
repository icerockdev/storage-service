/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.preview

import com.icerockdev.service.storage.s3.IS3Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

class PreviewService(
    private val storage: IS3Storage,
    private val srcBucket: String,
    private val dstBucket: String = srcBucket,
    configure: Configuration.() -> Unit = {}
) {

    private val configuration: Configuration

    class Configuration {
        var operationParallel: Int = 4
        var previewPrefix: String = ""
    }

    init {
        configuration = Configuration().apply(configure)
    }

    /**
     * Generate preview name by basic key and preview config
     */
    fun getPreviewName(srcKey: String, preview: AbstractPreview): String {
        return if (configuration.previewPrefix.isEmpty()) {
            "${preview.prefix}/$srcKey"
        } else {
            "${configuration.previewPrefix}/${preview.prefix}/$srcKey"
        }
    }

    /**
     * Generate preview for basic key by selected preview list
     */
    suspend fun generatePreview(
        srcKey: String,
        previewConfig: Collection<AbstractPreview>,
        processing: AbstractPreview.(imageBytes: ByteArray) -> ByteArray = { imageBytes -> bound(imageBytes) }
    ): Boolean {
        if (previewConfig.isEmpty()) {
            return true
        }

        val imageBytes = withContext(Dispatchers.IO) {
            storage.get(srcBucket, srcKey)?.use { it.readBytes() }
        } ?: return false

        try {
            previewConfig.asFlow()
                .buffer(configuration.operationParallel)
                .flowOn(Dispatchers.IO)
                .collect {
                    val preview = it.processing(imageBytes)
                    val dstKey = getPreviewName(srcKey, it)

                    storage.put(dstBucket, dstKey, preview)
                }

        } catch (e: Exception) {
            logger.error(e.localizedMessage, e)
            return false
        }

        return true
    }

    /**
     * Delete preview for basic key by selected preview list
     */
    suspend fun deletePreview(srcKey: String, previewConfig: Collection<AbstractPreview>): Boolean {
        if (previewConfig.isEmpty()) {
            return true
        }

        try {
            previewConfig.asFlow()
                .buffer(configuration.operationParallel)
                .flowOn(Dispatchers.IO)
                .collect {
                    val dstKey = getPreviewName(srcKey, it)
                    storage.delete(dstBucket, dstKey)
                }

        } catch (e: Exception) {
            logger.error(e.localizedMessage, e)
            return false
        }

        return true
    }


    companion object {
        private val logger = LoggerFactory.getLogger(PreviewService::class.java)
    }
}
