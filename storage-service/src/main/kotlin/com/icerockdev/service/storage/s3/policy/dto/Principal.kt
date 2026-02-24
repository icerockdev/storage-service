package com.icerockdev.service.storage.s3.policy.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Principal(
    @JsonProperty("AWS")
    @JsonDeserialize(using = PrincipalAwsDeserializer::class)
    val aws: List<String>,
    @JsonProperty("CanonicalUser")
    val canonicalUser: String?,
    @JsonProperty("Federated")
    val federated: String?,
    @JsonProperty("Service")
    val service: List<String> = emptyList(),
)

class PrincipalAwsDeserializer : JsonDeserializer<List<String>>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): List<String> {
        try {
            val path = p?.parsingContext?.pathAsPointer()
            val node = p?.codec?.readTree<JsonNode>(p)
            return when {
                node?.isNull == true -> emptyList()
                node?.isArray == true -> p.codec?.treeToValue(node, Array<String>::class.java)?.toList() ?: emptyList()
                node?.isTextual == true -> p.codec?.treeToValue(node, String::class.java)?.let { listOf(it) } ?: emptyList()
                else -> throw IllegalArgumentException("Principal deserialization error. Unexpected value: $node at path: $path")
            }
        } catch (e: JsonProcessingException) {
            throw RuntimeException("Principal deserialization error happened at: ${e.location}", e)
        }
    }
}
