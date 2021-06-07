package com.icerockdev.service.storage.s3.policy.builder

import com.icerockdev.service.storage.s3.policy.dto.EffectEnum
import com.icerockdev.service.storage.s3.policy.dto.Principal
import com.icerockdev.service.storage.s3.policy.dto.Statement

class StatementBuilder() {
    var effect: EffectEnum? = null
    var action: List<String>? = null
    var resource: List<String>? = null
    var principal: Principal? = null
    var condition: String? = null

    fun build() = Statement(effect = effect!!, action = action!!, principal = principal!!, resource = resource, condition = condition)
}
