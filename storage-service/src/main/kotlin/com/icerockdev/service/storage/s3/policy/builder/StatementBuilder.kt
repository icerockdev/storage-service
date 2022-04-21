package com.icerockdev.service.storage.s3.policy.builder

import com.icerockdev.service.storage.exception.S3StorageException
import com.icerockdev.service.storage.s3.policy.dto.ActionEnum
import com.icerockdev.service.storage.s3.policy.dto.EffectEnum
import com.icerockdev.service.storage.s3.policy.dto.Principal
import com.icerockdev.service.storage.s3.policy.dto.Resource
import com.icerockdev.service.storage.s3.policy.dto.Statement

class StatementBuilder {
    var effect: EffectEnum? = null
    var action: MutableList<ActionEnum> = mutableListOf()
    var notAction: MutableList<ActionEnum> = mutableListOf()
    var resource: MutableList<String> = mutableListOf()
    var notResource: MutableList<String> = mutableListOf()
    var principal: Principal? = null
    var notPrincipal: Principal? = null
    var condition: String? = null

    fun build(): Statement {
        val effect = effect
        if (effect === null) {
            throw S3StorageException("Effect must be filled")
        }
        if (action.isEmpty() && notAction.isEmpty()) {
            throw S3StorageException("Action or NotAction must be filled")
        }
        if (principal === null && notPrincipal === null) {
            throw S3StorageException("Principal or NotPrincipal must be filled")
        }
        if (resource.isEmpty() && notResource.isEmpty()) {
            throw S3StorageException("Resource or NotResource must be filled")
        }

        return Statement(
            effect = effect.actionName,
            action = action.map { it.actionName },
            notAction = notAction.map { it.actionName },
            principal = principal,
            notPrincipal = notPrincipal,
            resource = resource,
            notResource = notResource,
            condition = condition
        )
    }
}
