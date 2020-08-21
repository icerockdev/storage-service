/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.storage.dto

import java.io.InputStream

data class S3ObjectDto(
    override var id: Int? = null,
    override var stream: InputStream? = null,
    override var key: String? = null,
    override var entityId: Int? = null,
    override var size: Long? = null,
    override var entityType: String? = null,
    override var uuid: String? = null,
    override var metaData: ObjectMetaDataDto? = null,
    override var extension: String? = null,
    override var mimeType: String? = null,
    override var createdAt: String? = null,
    override var updatedAt: String? = null,
    val bucket: String? = null,
    val expiration: String? = null,
    val eTag: String? = null,
    val serverSideEncryption: String? = null,
    val versionId: String? = null,
    val SSECustomerAlgorithm: String? = null,
    val sSECustomerKeyMD5: String? = null,
    val sSEKMSKeyId: String? = null,
    val requestCharged: String? = null,
    val requestId: String? = null,
    val objectURL: String? = null
) :
    ObjectDto(
        id,
        stream,
        key,
        entityId,
        size,
        entityType,
        uuid,
        metaData,
        extension,
        mimeType,
        createdAt,
        updatedAt
    )
