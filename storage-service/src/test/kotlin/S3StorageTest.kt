/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import com.icerockdev.service.storage.s3.S3StorageImpl
import com.icerockdev.service.storage.s3.IS3Storage
import com.icerockdev.service.storage.s3.minioConfBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import kotlin.math.min
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class S3StorageTest {

    private val bucketName = "test"
    private val objectPath = "/home/alexsh/2.jpg"

    @Before
    fun init() {
        // TODO: load credentials from env
        s3 = S3Client.builder()
            .serviceConfiguration(minioConfBuilder)
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

        storage = S3StorageImpl(s3)
    }

    @Test
    fun testBucket() {
        assertFalse {
            storage.bucketExist(bucketName)
        }

        assertFalse {
            storage.deleteBucket(bucketName)
        }

        assertTrue {
            storage.createBucket(bucketName)
        }

        assertTrue {
            storage.bucketExist(bucketName)
        }

        assertTrue {
            storage.deleteBucket(bucketName)
        }
    }

    @Test
    fun testPutExistCopyDelete() {
        // init storage
        if (!storage.bucketExist(bucketName)) {
            storage.createBucket(bucketName)
        }

        val fileName = storage.generateFileKey()
        val stream = FileInputStream(File(objectPath))

        // check wrong cases
        assertFalse {
            storage.objectExists(bucketName, fileName)
        }

        assertTrue {
            storage.delete(bucketName, fileName)
        }

        // set obj
        assertTrue {
            storage.put(bucketName, fileName, stream)
        }

        // override
        assertTrue {
            storage.put(bucketName, fileName, stream) // store empty file (no input reset)
        }

        stream.close()

        // check success cases
        assertTrue {
            storage.objectExists(bucketName, fileName)
        }

        // copy testing
        val copyFileName = storage.generateFileKey()
        val copyBucket = "test2"
        // copy to current bucket
        assertTrue {
            storage.copy(bucketName, fileName, bucketName, copyFileName)
        }

        assertFalse {
            storage.copy(bucketName, fileName, copyBucket, copyFileName)
        }

        if (!storage.bucketExist(copyBucket)) {
            storage.createBucket(copyBucket)
        }

        assertTrue {
            storage.copy(bucketName, fileName, copyBucket, copyFileName)
        }

        // drop copy
        assertTrue {
            storage.delete(bucketName, copyFileName)
        }

        assertTrue {
            storage.delete(copyBucket, copyFileName)
        }

        assertTrue {
            storage.deleteBucket(copyBucket)
        }

        // check delete full bucket
        assertFails {
            // non empty bucket
            storage.deleteBucket(bucketName)
        }

        assertFalse {
            storage.deleteBucket("another bucket")
        }

        // check correct delete
        assertTrue {
            storage.delete(bucketName, fileName)
        }

        assertFalse {
            storage.objectExists(bucketName, fileName)
        }

        assertTrue {
            storage.deleteBucket(bucketName)
        }
    }

    @Test
    fun testGetListDeleteAll() {
        // init storage
        if (!storage.bucketExist(bucketName)) {
            storage.createBucket(bucketName)
        }

        val fileName1 = storage.generateFileKey()
        val fileName2 = "temp/somefile"

        val file = File(objectPath)
        var stream = FileInputStream(file)

        storage.put(bucketName, fileName1, stream)
        stream.close()

        stream = FileInputStream(file)
        storage.put(bucketName, fileName2, stream)
        stream.close()

        val list = storage.list(bucketName, "")
        assertEquals(2, list.size)

        val listPrefix = storage.list(bucketName, "temp")
        assertEquals(1, listPrefix.size)

        val loadFile = storage.get(bucketName, fileName1)
        assertNotNull(loadFile)

        stream = FileInputStream(file)
        assertTrue {
            isEqual(loadFile, stream)
        }
        stream.close()

        // drop all from bucket
        assertTrue {
            storage.deleteBucketWithObjects(bucketName)
        }
    }

    @After
    fun close() {
        s3.close()
    }

    @Throws(IOException::class)
    private fun isEqual(i1: InputStream, i2: InputStream): Boolean {
        val ch1: ReadableByteChannel = Channels.newChannel(i1)
        val ch2: ReadableByteChannel = Channels.newChannel(i2)
        val buf1: ByteBuffer = ByteBuffer.allocateDirect(1024)
        val buf2: ByteBuffer = ByteBuffer.allocateDirect(1024)
        try {
            while (true) {
                val n1 = ch1.read(buf1)
                val n2 = ch2.read(buf2)
                if (n1 == -1 || n2 == -1) return n1 == n2
                buf1.flip()
                buf2.flip()
                for (i in 0 until min(n1, n2)) if (buf1.get() !== buf2.get()) return false
                buf1.compact()
                buf2.compact()
            }
        } finally {
            i1.close()
            i2.close()
        }
    }

    companion object {
        private val region = Region.US_WEST_2

        private lateinit var s3: S3Client
        private lateinit var storage: IS3Storage
    }
}
