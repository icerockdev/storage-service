package com.icerockdev.service.storage.s3.policy.dto

enum class ResourceEnum(var resourceName: String) {
    ALL("arn:aws:s3:::*"),
    ALL_IN_TEST("arn:aws:s3:::test-bucket/*"),
    FROM_RANGE("arn:aws:s3:::test-bucket/");

    fun addNum(num: Int): String {
        return "${this.resourceName}$num"
    }
}