package com.icerockdev.service.storage.s3.policy.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Principal(
    @JsonProperty("AWS")
    val aws: List<String>,
    @JsonProperty("CanonicalUser")
    val canonicalUser: String?,
    @JsonProperty("Federated")
    val federated: String?,
    @JsonProperty("Service")
    val service: List<String> = emptyList(),
)
