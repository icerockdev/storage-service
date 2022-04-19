package com.icerockdev.service.storage.s3.policy.builder

import com.icerockdev.service.storage.exception.S3StorageException
import com.icerockdev.service.storage.s3.policy.dto.Principal

class PrincipalBuilder() {
    var aws: MutableList<String> = mutableListOf()
    var canonicalUser: String? = null
    var federated: String? = null
    var service: MutableList<String> = mutableListOf()

    fun build(): Principal {
        if (aws.isEmpty() && canonicalUser === null && federated === null && service.isEmpty()) {
            throw S3StorageException("Principal configuration not filled")
        }

        return Principal(aws = aws, canonicalUser = canonicalUser, federated = federated, service = service)
    }
}
