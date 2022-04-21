package com.icerockdev.service.storage.mime

data class MimeType(
    private val baseType: String
) {
    override fun toString(): String {
        return baseType
    }
}
