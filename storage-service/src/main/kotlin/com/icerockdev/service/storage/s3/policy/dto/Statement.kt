package com.icerockdev.service.storage.s3.policy.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Statement(
    @JsonProperty("Effect")
    val effect: String,
    @JsonProperty("Principal")
    val principal: Principal?,
    @JsonProperty("NotPrincipal")
    val notPrincipal: Principal?,
    @JsonProperty("Action")
    val action: List<String> = emptyList(),
    @JsonProperty("NotAction")
    val notAction: List<String> = emptyList(),
    @JsonProperty("Resource")
    val resource: List<String> = emptyList(),
    @JsonProperty("NotResource")
    val notResource: List<String> = emptyList(),
    @JsonProperty("Condition")
    val condition: String?,
)
