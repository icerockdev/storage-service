package com.icerockdev.service.storage.s3.policy.dto

enum class EffectEnum(val actionName: String) {
    ALLOW("Allow"),
    DENY("Deny")
}
