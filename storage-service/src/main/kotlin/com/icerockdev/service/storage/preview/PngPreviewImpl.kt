/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.preview

import com.sksamuel.scrimage.nio.ImageWriter
import com.sksamuel.scrimage.nio.PngWriter

class PngPreviewImpl(
    width: Int?,
    height: Int?,
    private val compression: Int = 9, // between 0 and 9, 9 - max compression
    override val scaleMethod: PreviewScaleMethod = PreviewScaleMethod.Bicubic
) : AbstractPreview(width, height) {

    init {
        if (compression > 9 || compression < 0) {
            throw PreviewException("Invalid configuration of compression")
        }
    }

    override fun getWriter(): ImageWriter {
        return PngWriter().withCompression(compression)
    }
}
