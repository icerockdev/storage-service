package com.icerockdev.service.storage.s3.policy.builder

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.icerockdev.service.storage.s3.policy.dto.EffectEnum
import com.icerockdev.service.storage.s3.policy.dto.Policy
import com.icerockdev.service.storage.s3.policy.dto.Principal
import com.icerockdev.service.storage.s3.policy.dto.Statement
import java.text.SimpleDateFormat
import java.util.Date

class PolicyBuilder {
    var id: String? = null
    var statement: MutableList<Statement> = mutableListOf()
    var sid: String? = null

    fun withStatement(value: Statement) = apply { statement.add(value) }

    fun build(): String {
        return jacksonObjectMapper().writeValueAsString(
            Policy(
                version = SimpleDateFormat("yyyy-MM-dd").format(Date()),
                id = id,
                statement = statement!!,
                sid = sid,
            )
        )
    }
}
