import com.icerockdev.service.storage.storage.S3Storage
import com.icerockdev.service.storage.storage.Storage
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI

/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

class StorageTest {


    @Test
    fun testPut() {

    }

    companion object {
        private val region = Region.US_WEST_2

        private val confBuilder =
            S3Configuration
                .builder()
                .pathStyleAccessEnabled(true)
                .checksumValidationEnabled(false)
                .build()

        private lateinit var s3: S3Client
        private lateinit var storage: Storage

        @BeforeClass
        fun init() {
            // TODO: load credentials from env
            s3 = S3Client.builder()
                .serviceConfiguration(confBuilder)
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                            "my_access_key", "my_secret_key"
                        )
                    )
                )
                .endpointOverride(URI.create("http://127.0.0.30:9000"))
                .region(region)
                .build()

            storage = S3Storage(s3)
        }

        @AfterClass
        fun close() {
            s3.close()
        }
    }
}