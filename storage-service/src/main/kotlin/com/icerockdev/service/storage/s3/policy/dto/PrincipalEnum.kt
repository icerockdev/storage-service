package com.icerockdev.service.storage.s3.policy.dto

/**
 * **See Also:** [Principal](https://docs.aws.amazon.com/AmazonS3/latest/userguide/s3-bucket-user-policy-specifying-principal-intro.html)
 */
enum class PrincipalEnum(val accessName: String) {
    PUBLIC_ACCESS("*"),
}
