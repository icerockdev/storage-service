package com.icerockdev.service.storage.s3.policy.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Policy(
    @JsonProperty("Version")
    val version: String,
    @JsonProperty("Statement")
    val statement: List<Statement>,
    @JsonProperty("Id")
    val id: String?,
    @JsonProperty("Sid")
    val sid: String?,
)
