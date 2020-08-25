/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.preview

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.ScaleMethod
import com.sksamuel.scrimage.nio.ImageWriter

// TODO: implements more methods https://github.com/sksamuel/scrimage
abstract class AbstractPreview(
    private val width: Int?,
    private val height: Int?
) {
    protected abstract val scaleMethod: ScaleMethod
    var prefix = "${width ?: "n"}x${height ?: "n"}"

    init {
        if (width === null && height === null) {
            throw PreviewException("Invalid configuration of size")
        }
    }

    fun bound(imageByteArray: ByteArray): ByteArray {
        return ImmutableImage.loader().fromBytes(imageByteArray).bound(width ?: Int.MAX_VALUE, height ?: Int.MAX_VALUE, scaleMethod).bytes(getWriter())
    }

    protected abstract fun getWriter(): ImageWriter
}