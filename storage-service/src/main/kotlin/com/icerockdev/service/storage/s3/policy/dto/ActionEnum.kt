package com.icerockdev.service.storage.s3.policy.dto

enum class ActionEnum(val actionName: String) {
    All("s3:*"),
    AbortMultipartUpload("s3:AbortMultipartUpload"),
    CreateBucket("s3:CreateBucket"),
    DeleteBucket("s3:DeleteBucket"),
    ForceDeleteBucket("s3:ForceDeleteBucket"),
    DeleteBucketPolicy("s3:DeleteBucketPolicy"),
    DeleteObject("s3:DeleteObject"),
    GetBucketLocation("s3:GetBucketLocation"),
    GetBucketNotification("s3:GetBucketNotification"),
    GetBucketPolicy("s3:GetBucketPolicy"),
    GetObject("s3:GetObject"),
    HeadBucket("s3:HeadBucket"),
    ListAllMyBuckets("s3:ListAllMyBuckets"),
    ListBucket("s3:ListBucket"),
    ListMultipartUploads("s3:ListMultipartUploads"),
    ListenNotification("s3:ListenNotification"),
    ListenBucketNotification("s3:ListenBucketNotification"),
    ListParts("s3:ListParts"),
    PutBucketLifecycle("s3:PutBucketLifecycle"),
    GetBucketLifecycle("s3:GetBucketLifecycle"),
    PutObjectNotification("s3:PutObjectNotification"),
    PutBucketPolicy("s3:PutBucketPolicy"),
    PutObject("s3:PutObject"),
    DeleteObjectVersion("s3:DeleteObjectVersion"),
    DeleteObjectVersionTagging("s3:DeleteObjectVersionTagging"),
    GetObjectVersion("s3:GetObjectVersion"),
    GetObjectVersionTagging("s3:GetObjectVersionTagging"),
    PutObjectVersionTagging("s3:PutObjectVersionTagging"),
    BypassGovernanceRetention("s3:BypassGovernanceRetention"),
    PutObjectRetention("s3:PutObjectRetention"),
    GetObjectRetention("s3:GetObjectRetention"),
    GetObjectLegalHold("s3:GetObjectLegalHold"),
    PutObjectLegalHold("s3:PutObjectLegalHold"),
    GetBucketObjectLockConfiguration("s3:GetBucketObjectLockConfiguration"),
    PutBucketObjectLockConfiguration("s3:PutBucketObjectLockConfiguration"),
    GetBucketTagging("s3:GetBucketTagging"),
    PutBucketTagging("s3:PutBucketTagging"),
    Get("s3:Get"),
    Put("s3:Put"),
    Delete("s3:Delete"),
    PutEncryptionConfiguration("s3:PutEncryptionConfiguration"),
    GetEncryptionConfiguration("s3:GetEncryptionConfiguration"),
    PutBucketVersioning("s3:PutBucketVersioning"),
    GetBucketVersioning("s3:GetBucketVersioning"),
    GetReplicationConfiguration("s3:GetReplicationConfiguration"),
    PutReplicationConfiguration("s3:PutReplicationConfiguration"),
    ReplicateObject("s3:ReplicateObject"),
    ReplicateDelete("s3:ReplicateDelete"),
    ReplicateTags("s3:ReplicateTags"),
    GetObjectVersionForReplication("s3:GetObjectVersionForReplication")
}