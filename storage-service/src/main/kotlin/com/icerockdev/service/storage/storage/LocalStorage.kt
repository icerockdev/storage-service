/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.storage

import com.icerockdev.service.storage.dto.ObjectDto
import java.io.InputStream

class LocalStorage : Storage {
    override fun get(key: String): ObjectDto {
        TODO("Not yet implemented")
    }

    override fun list(prefix: String): List<ObjectDto> {
        TODO("Not yet implemented")
    }

    override fun isTargetExist(key: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun isObjectExists(key: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun put(key: String, stream: InputStream): ObjectDto {
        TODO("Not yet implemented")
    }

    override fun copy(key: String, stream: InputStream): ObjectDto {
        TODO("Not yet implemented")
    }

    override fun delete(key: String): Boolean {
        TODO("Not yet implemented")
    }

}
