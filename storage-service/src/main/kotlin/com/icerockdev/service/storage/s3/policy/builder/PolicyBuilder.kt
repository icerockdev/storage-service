package com.icerockdev.service.storage.s3.policy.builder

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.icerockdev.service.storage.s3.policy.dto.Policy
import com.icerockdev.service.storage.s3.policy.dto.Statement
import java.lang.Exception

class PolicyBuilder {
    var id: String? = null
    var statement: MutableList<Statement> = mutableListOf()
    var sid: String? = null
    private val actualPolicyLanguageVersion = "2012-10-17"

    fun build(): String {
        if (statement.isEmpty()) {
            throw Exception("Statement in empty")
        }

        return jacksonObjectMapper().writeValueAsString(
            Policy(
                version = actualPolicyLanguageVersion,
                id = id,
                statement = statement,
                sid = sid,
            )
        )
    }
}
