package com.icerockdev.service.storage.s3.policy.builder

import com.icerockdev.service.storage.exception.S3StorageException

class ResourceBuilder {
    var bucket: String = "*"
    var detailRoute: String = "*"
    var defaultResource: String = "arn:aws:s3:::"

    fun build(): String {

        if (bucket == "*" && detailRoute != "*")
            throw S3StorageException("If resource has a detail route, then it must have a bucket")

        if (bucket == "*" && detailRoute == "*")
            return "$defaultResource*"

        if (bucket != "*" && detailRoute == "*")
            return "$defaultResource$bucket/*"

        return "$defaultResource$bucket/$detailRoute"
    }
}
