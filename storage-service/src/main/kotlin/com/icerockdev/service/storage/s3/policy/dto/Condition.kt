package com.icerockdev.service.storage.s3.policy.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Condition(
    @JsonProperty("StringEquals")
    val stringEquals: HashMap<String, String>,
    @JsonProperty("StringEqualsIgnoreCase")
    val stringEqualsIgnoreCase: HashMap<String, String>,
)
