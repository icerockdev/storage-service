/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.preview

import com.sksamuel.scrimage.ImmutableImage

/**
 * Load image from bytes, just helper
 */
fun loadImage(imageByteArray: ByteArray): ImmutableImage {
    return ImmutableImage.loader().fromBytes(imageByteArray)
}

/**
 * Bound image by AbstractPreview params, more methods here https://github.com/sksamuel/scrimage
 * TODO: make more helpers
 */
fun AbstractPreview.boundImage(imageBytes: ByteArray): ByteArray {
    return loadImage(imageBytes).bound(getWidthOrMax(), getHeightOrMax(), scaleMethod).bytes(getWriter())
}
