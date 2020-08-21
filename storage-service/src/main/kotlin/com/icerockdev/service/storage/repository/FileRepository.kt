/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.repository

import com.icerockdev.service.storage.dto.ObjectDto

interface FileRepository {
    fun get(key: String): ObjectDto?

    fun getQuantity(entityId: Int, type: String): Int

    fun getEntityName(id: Int): String?

    fun getContentTypeId(name: String): Int?

    fun getContentTypeName(id: Int): String?

    fun list(
        prefix: String = "",
        limit: Int? = null,
        offset: Int? = null,
        sortBy: String? = "id",
        orderBy: String? = "DESC"
    ): List<ObjectDto>

    fun getListByEntityId(entityId: Int): List<ObjectDto>

    fun getListByEntityIdAndType(entityId: Int, type: String): List<ObjectDto>

    fun create(dto: ObjectDto): Boolean

    fun update(dto: ObjectDto): Int

    fun delete(key: String): Boolean
}
