/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.preview

import com.icerockdev.service.storage.s3.IS3Storage
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.JpegWriter
import java.io.FilterInputStream

fun scaleImage(filterInputStream: FilterInputStream): ByteArray {
    val writer = JpegWriter().withCompression(50).withProgressive(true)
    val image = ImmutableImage.loader().fromStream(filterInputStream)

    image.width

    return image.bound(1000, 50).bytes(writer)
}

fun IS3Storage.generateAndStorePreview(srcBucket: String, srcKey: String, preview: String, dstKey: String): Boolean {
    val inputStream = get(srcBucket, srcKey)
    if (inputStream === null) {
        return false
    }

    val imageBytes = scaleImage(inputStream)

    return put(preview, dstKey, imageBytes)
}