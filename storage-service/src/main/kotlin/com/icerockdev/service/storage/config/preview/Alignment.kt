/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.config.preview

enum class VerticalAlignment(val alignment: String) {
    TOP("top"),
    BOTTOM("bottom"),
}

enum class HorizontalAlignment(val alignment: String) {
    LEFT("left"),
    RIGHT("right"),
}

class Position(verticalAlignment: VerticalAlignment, horizontalAlignment: HorizontalAlignment)
