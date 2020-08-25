/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.preview

import com.sksamuel.scrimage.ScaleMethod
import com.sksamuel.scrimage.nio.ImageWriter

abstract class AbstractPreview(
    val width: Int?,
    val height: Int?
) {
    abstract val scaleMethod: ScaleMethod
    var prefix = "${width ?: "n"}x${height ?: "n"}"

    init {
        if (width === null && height === null) {
            throw PreviewException("Invalid configuration of size")
        }
    }

    fun getWidthOrMax(): Int {
        return width ?: Int.MAX_VALUE
    }

    fun getHeightOrMax(): Int {
        return height ?: Int.MAX_VALUE
    }

    abstract fun getWriter(): ImageWriter
}
