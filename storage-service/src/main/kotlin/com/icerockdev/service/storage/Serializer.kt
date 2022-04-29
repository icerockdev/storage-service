package com.icerockdev.service.storage

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

object Serializer {
    val objectMapper: ObjectMapper = jacksonObjectMapper()

    fun serialize(obj: Any): String {
        return objectMapper.writeValueAsString(obj)
    }

    inline fun <reified T> deserialize(content: String): T {
        return objectMapper.readValue(content = content)
    }
}
