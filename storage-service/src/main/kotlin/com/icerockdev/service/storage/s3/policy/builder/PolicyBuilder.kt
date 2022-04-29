package com.icerockdev.service.storage.s3.policy.builder

import com.icerockdev.service.storage.Serializer
import com.icerockdev.service.storage.exception.S3StorageException
import com.icerockdev.service.storage.s3.policy.dto.Policy
import com.icerockdev.service.storage.s3.policy.dto.Statement

/**
 * **See Also:** [Bucket policy examples](https://docs.aws.amazon.com/AmazonS3/latest/userguide/example-bucket-policies.html)
 */
class PolicyBuilder {
    var id: String? = null
    var statement: MutableList<Statement> = mutableListOf()
    var sid: String? = null
    private val actualPolicyLanguageVersion = "2012-10-17"
    private val policySizeLimit = 20_480

    fun build(): String {
        if (statement.isEmpty()) {
            throw S3StorageException("Statement in empty")
        }

        val policy = Serializer.serialize(
            Policy(
                version = actualPolicyLanguageVersion,
                id = id,
                statement = statement,
                sid = sid,
            )
        )

        if (policy.toByteArray().size > policySizeLimit) {
            throw S3StorageException("Policy exceeds the maximum allowed document size")
        }

        return policy
    }
}
