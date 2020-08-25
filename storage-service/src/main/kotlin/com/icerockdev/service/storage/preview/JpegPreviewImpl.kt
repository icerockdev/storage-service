/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.preview

import com.sksamuel.scrimage.ScaleMethod
import com.sksamuel.scrimage.nio.ImageWriter
import com.sksamuel.scrimage.nio.JpegWriter

class JpegPreviewImpl(
    width: Int?,
    height: Int?,
    private val compression: Int = 80,
    private val progressive: Boolean = false,
    override val scaleMethod: ScaleMethod = ScaleMethod.Bicubic
) : AbstractPreview(width, height) {

    init {
        if (compression > 100 || compression <= 0) {
            throw PreviewException("Invalid configuration of compression")
        }
    }

    override fun getWriter(): ImageWriter {
        return JpegWriter().withCompression(compression).withProgressive(progressive)
    }
}