package com.icerockdev.service.storage.s3.policy.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Statement(
    @JsonProperty("Effect")
    val effect: EffectEnum,
    @JsonProperty("Principal")
    val principal: Principal?,
    @JsonProperty("NotPrincipal")
    val notPrincipal: Principal?,
    @JsonProperty("Action")
    val action: List<String>,
    @JsonProperty("NotAction")
    val notAction: List<String>,
    @JsonProperty("Resource")
    val resource: List<String>?,
    @JsonProperty("NotResource")
    val notResource: List<String>?,
    @JsonProperty("Condition")
    val condition: String?,
)
