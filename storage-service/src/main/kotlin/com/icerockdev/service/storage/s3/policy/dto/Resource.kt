package com.icerockdev.service.storage.s3.policy.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Resource(
    @JsonProperty("Bucket")
    val bucket: String?,
    @JsonProperty("DetailRoute")
    val detailRoute: String?,
)
