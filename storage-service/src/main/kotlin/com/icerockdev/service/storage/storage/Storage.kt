/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.storage

import com.icerockdev.service.storage.dto.ObjectDto
import java.io.InputStream

interface Storage {
    fun get(key: String): ObjectDto

    fun list(prefix: String): List<ObjectDto>

    fun isTargetExist(key: String): Boolean

    fun isObjectExists(key: String): Boolean

    fun put(key: String, stream: InputStream): ObjectDto

    fun copy(key: String, stream: InputStream): ObjectDto

    fun delete(key: String): Boolean
}
