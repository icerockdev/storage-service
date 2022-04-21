/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.preview

import java.util.concurrent.ConcurrentHashMap

class PreviewConfig {
    private val map: MutableMap<String, AbstractPreview> = ConcurrentHashMap<String, AbstractPreview>()

    fun append(alias: String, preview: AbstractPreview) {
        map[alias] = preview
    }

    fun getPreviewList(): List<AbstractPreview> {
        return map.values.toList()
    }

    fun getAliasList(): List<String> {
        return map.keys.toList()
    }

    fun getConfigByAlias(alias: String): AbstractPreview? {
        return map[alias]
    }
}
