package com.icerockdev.service.storage.s3.policy.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Statement(
    @JsonProperty("Effect")
    val effect: EffectEnum,
    @JsonProperty("Principal")
    var principal: Principal,
//    NotPrincipal // AWS JSON policy elements
    @JsonProperty("Action")
    val action: List<String>,
//    NotAction  // IAM JSON policy elements
    @JsonProperty("Resource")
    val resource: List<String>?,
//    NotResource  // IAM JSON policy elements
    @JsonProperty("Condition")
    val condition: String?,
)
