package com.icerockdev.service.storage.s3.policy.builder

import com.icerockdev.service.storage.s3.policy.dto.Principal

class PrincipalBuilder() {
    var aws: MutableList<String> = mutableListOf()
    var canonicalUser: String? = null
    var federated: String? = null
    var service: MutableList<String> = mutableListOf()

    fun build() = Principal(aws = aws, canonicalUser = canonicalUser, federated = federated, service = service)
}
