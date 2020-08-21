/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.dto

import java.io.InputStream

abstract class ObjectDto(
    open var id: Int? = null,
    open var stream: InputStream? = null,
    open var key: String? = null,
    open var entityId: Int? = null,
    open var size: Long? = null,
    open var entityType: String? = null,
    open var uuid: String? = null,
    open var metaData: ObjectMetaDataDto? = null,
    open var extension: String? = null,
    open var mimeType: String? = null,
    open var createdAt: String? = null,
    open var updatedAt: String? = null
)
