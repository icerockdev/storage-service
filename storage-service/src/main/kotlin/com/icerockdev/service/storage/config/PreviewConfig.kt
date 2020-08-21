/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.config

import com.icerockdev.service.storage.config.preview.HorizontalAlignment
import com.icerockdev.service.storage.config.preview.Margin
import com.icerockdev.service.storage.config.preview.Position
import com.icerockdev.service.storage.config.preview.Size
import com.icerockdev.service.storage.config.preview.VerticalAlignment

const val DEFAULT_PREVIEW_QUALITY = 60
const val DEFAULT_PREVIEW_WIDTH = 100
const val DEFAULT_PREVIEW_HEIGHT = 100
const val DEFAULT_STAMP_PART_SIZE = 0.1
const val DEFAULT_STAMP_MARGIN_X = 10
const val DEFAULT_STAMP_MARGIN_Y = 10

class PreviewConfig(
        val path: String,
        val quality: Int = DEFAULT_PREVIEW_QUALITY,
        val sizes: List<Size> = listOf(Size(DEFAULT_PREVIEW_WIDTH, DEFAULT_PREVIEW_HEIGHT)),
        val stampPartSize: Double = DEFAULT_STAMP_PART_SIZE,
        val stampPosition: Position = Position(VerticalAlignment.BOTTOM, HorizontalAlignment.RIGHT),
        val stampMargin: Margin = Margin(DEFAULT_STAMP_MARGIN_X, DEFAULT_STAMP_MARGIN_Y)
)
