/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import com.fasterxml.jackson.module.kotlin.readValue
import com.icerockdev.service.storage.exception.S3StorageException
import com.icerockdev.service.storage.mime.MimeTypeDetector
import com.icerockdev.service.storage.s3.IS3Storage
import com.icerockdev.service.storage.s3.S3StorageImpl
import com.icerockdev.service.storage.s3.minioConfBuilder
import com.icerockdev.service.storage.s3.policy.dto.ActionEnum
import com.icerockdev.service.storage.s3.policy.dto.EffectEnum
import com.icerockdev.service.storage.s3.policy.dto.Policy
import com.icerockdev.service.storage.s3.policy.dto.PrincipalEnum
import com.icerockdev.service.storage.serializer
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.time.Duration
import kotlin.math.min
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


class S3StorageTest {
    private val dotenv = dotenv {
        directory = "../"
    }
    private val bucketName = dotenv["S3_BUCKET"]!!
    private val classLoader = javaClass.classLoader

    @Before
    fun init() {
        s3 = S3Client.builder()
            .serviceConfiguration(minioConfBuilder)
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        dotenv["MINIO_ACCESS_KEY"], dotenv["MINIO_SECRET_KEY"]
                    )
                )
            )
            .endpointOverride(URI.create(dotenv["S3_ENDPOINT"]!!))
            .region(Region.of(dotenv["S3_REGION"]))
            .build()

        preSigner = S3Presigner.builder()
            .serviceConfiguration(minioConfBuilder)
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        dotenv["MINIO_ACCESS_KEY"], dotenv["MINIO_SECRET_KEY"]
                    )
                )
            )
            .endpointOverride(URI.create(dotenv["S3_ENDPOINT"]!!))
            .region(Region.of(dotenv["S3_REGION"]))
            .build()

        storage = S3StorageImpl(s3, preSigner)
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

        val (fileName, stream) = getFile(FileType.JPG)

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
    fun testPutMimeType() {
        // init storage
        if (!storage.bucketExist(bucketName)) {
            storage.createBucket(bucketName)
        }

        val (jpgFileName, jpgStream) = getFile(FileType.JPG)
        val (pngFileName, pngStream) = getFile(FileType.PNG)
        val (gifFileName, gifStream) = getFile(FileType.GIF)

        // Check wrong cases
        assertFalse(storage.objectExists(bucketName, jpgFileName))
        assertFalse(storage.objectExists(bucketName, pngFileName))
        assertFalse(storage.objectExists(bucketName, gifFileName))

        // Put object to storage
        assertTrue(storage.put(bucketName, jpgFileName, jpgStream))
        assertTrue(storage.put(bucketName, pngFileName, pngStream))
        assertTrue(storage.put(bucketName, gifFileName, gifStream))

        jpgStream.close()
        pngStream.close()
        gifStream.close()

        // Check object exist
        assertTrue(storage.objectExists(bucketName, jpgFileName))
        assertTrue(storage.objectExists(bucketName, pngFileName))
        assertTrue(storage.objectExists(bucketName, gifFileName))

        val jpgObject = storage.get(bucketName, jpgFileName)
        val pngObject = storage.get(bucketName, pngFileName)
        val gifObject = storage.get(bucketName, gifFileName)

        assertEquals("image/jpeg", jpgObject?.response()?.contentType())
        assertEquals("image/png", pngObject?.response()?.contentType())
        assertEquals("image/gif", gifObject?.response()?.contentType())

        assertTrue(storage.delete(bucketName, jpgFileName))
        assertTrue(storage.delete(bucketName, pngFileName))
        assertTrue(storage.delete(bucketName, gifFileName))

        assertTrue {
            storage.deleteBucket(bucketName)
        }
    }

    @Test
    fun testFileSizeAndType() {
        // init storage
        if (!storage.bucketExist(bucketName)) {
            storage.createBucket(bucketName)
        }

        val (jpgFileName, jpgStream) = getFile(FileType.JPG)
        val mimeType = MimeTypeDetector.detect(jpgStream)
        val jpgFileByteArray = jpgStream.readAllBytes()

        jpgStream.close()

        // Check wrong cases
        assertFalse(storage.objectExists(bucketName, jpgFileName))

        // Put object to storage
        assertTrue(storage.put(bucketName, jpgFileName, jpgFileByteArray.inputStream()))

        // Check object exist
        assertTrue(storage.objectExists(bucketName, jpgFileName))

        val jpgObject = storage.get(bucketName, jpgFileName)

        assertEquals("image/jpeg", mimeType.toString())
        assertEquals(mimeType.toString(), jpgObject?.response()?.contentType())

        assertEquals(jpgFileByteArray.size.toLong(), jpgObject?.response()?.contentLength())

        assertTrue {
            storage.deleteBucketWithObjects(bucketName)
        }
    }

    @Test
    fun testMetadata() {
        // init storage
        if (!storage.bucketExist(bucketName)) {
            storage.createBucket(bucketName)
        }

        val metadata = mapOf("attribute1" to "testValue1", "attribute2" to "testValue2")

        val (jpgFileName, jpgStream) = getFile(FileType.JPG)

        // Check wrong cases
        assertFalse(storage.objectExists(bucketName, jpgFileName))

        // Put object to storage
        assertTrue(storage.put(bucketName, jpgFileName, jpgStream, metadata))
        jpgStream.close()

        // Check object exist
        assertTrue(storage.objectExists(bucketName, jpgFileName))

        val jpgObject = storage.get(bucketName, jpgFileName)

        assertEquals(metadata, jpgObject?.response()?.metadata())

        // copy testing
        val copyFileName = storage.generateFileKey()
        // copy to current bucket
        assertTrue {
            storage.copy(bucketName, jpgFileName, bucketName, copyFileName)
        }

        val copyObject = storage.get(bucketName, copyFileName)

        assertEquals(metadata, copyObject?.response()?.metadata())

        assertTrue {
            storage.deleteBucketWithObjects(bucketName)
        }
    }

    @Test
    fun testShareGetURL() {
        // Init storage
        if (!storage.bucketExist(bucketName)) {
            storage.createBucket(bucketName)
        }

        val fileName = storage.generateFileKey()
        val stream = classLoader.getResourceAsStream(dotenv["JPG_TEST_OBJECT"])
            ?: throw NullPointerException("JPG File not found")

        // Check wrong cases
        assertFalse {
            storage.objectExists(bucketName, fileName)
        }

        // Put object
        assertTrue {
            storage.put(bucketName, fileName, stream)
        }

        assertEquals(
            "${dotenv["S3_ENDPOINT"]}/${bucketName}/${fileName}",
            storage.getUrl(URI.create(dotenv["S3_ENDPOINT"]!!), bucketName, fileName)
        )

        runBlocking {
            val url = storage.share(bucketName, fileName, Duration.ofSeconds(2L))

            // Test sharing
            val httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build()

            val httpRequest: HttpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url!!))
                .GET()
                .build()

            val successResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
            val successHeaders = successResponse.headers()
            assertEquals(200, successResponse.statusCode())
            assertEquals("image/jpeg", successHeaders.firstValue("content-type").get())

            Thread.sleep(2000)

            val failResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
            val failHeaders = failResponse.headers()
            assertEquals(403, failResponse.statusCode())
            assertEquals("application/xml", failHeaders.firstValue("content-type").get())
        }

        assertTrue {
            storage.delete(bucketName, fileName)
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

        val file = classLoader.getResource(dotenv["JPG_TEST_OBJECT"])?.file
            ?: throw NullPointerException("JPG File not found")
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

    @Test
    fun testMimeTypeDetect() {
        val (_, jpgStream) = getFile(FileType.JPG)
        val (_, pngStream) = getFile(FileType.PNG)
        val (_, gifStream) = getFile(FileType.GIF)
        val (_, pdfStream) = getFile(FileType.PDF)
        val (_, zipStream) = getFile(FileType.ZIP)
        val (_, binStream) = getFile(FileType.BIN)

        assertEquals("image/jpeg", MimeTypeDetector.detect(jpgStream).toString())
        assertEquals("image/png", MimeTypeDetector.detect(pngStream).toString())
        assertEquals("image/gif", MimeTypeDetector.detect(gifStream).toString())
        assertEquals("application/pdf", MimeTypeDetector.detect(pdfStream).toString())
        assertEquals("application/zip", MimeTypeDetector.detect(zipStream).toString())
        assertEquals("application/octet-stream", MimeTypeDetector.detect(binStream).toString())
    }

    @Test
    fun testPolicy() {
        if (!storage.bucketExist(bucketName)) {
            storage.createBucket(bucketName)
        }
        val testStatement = storage.buildStatement {
            effect = EffectEnum.ALLOW

            action.add(ActionEnum.DELETE_OBJECT)
            action.add(ActionEnum.GET_OBJECT)
            action.add(ActionEnum.PUT_OBJECT)

            resource.add(storage.buildResource {})

            principal = storage.buildPrincipal {
                aws.add(PrincipalEnum.PUBLIC_ACCESS.accessName)
            }
        }

        val putPolicyResult = storage.putBucketPolicy(bucketName) {
            statement.add(testStatement)
        }
        assertTrue(putPolicyResult)

        val policyStatement = storage.getBucketPolicy(bucketName)?.let {
            serializer.readValue<Policy>(it)
        }?.statement?.first()

        assertEquals(testStatement.action.sorted(), policyStatement?.action?.sorted())
        assertEquals(testStatement.resource, policyStatement?.resource)
        assertEquals(testStatement.effect, policyStatement?.effect)
        assertEquals(testStatement.principal, policyStatement?.principal)
        assertEquals(testStatement.principal?.aws, policyStatement?.principal?.aws)

        val currentPolicy = storage.getBucketPolicy(bucketName)
        assertNotNull(currentPolicy)

        val bigTestStatement = storage.buildStatement {
            effect = EffectEnum.ALLOW
            action.add(ActionEnum.GET_OBJECT)
            resource.add(
                storage.buildResource {
                    bucket = bucketName
                }
            )
            for (bucketNum in 1..1000) {
                resource.add(
                    storage.buildResource {
                        bucket = bucketName
                        detailRoute = bucketNum.toString()
                    }
                )
            }
            principal = storage.buildPrincipal {
                aws.add(PrincipalEnum.PUBLIC_ACCESS.accessName)
            }
        }
        assertFailsWith<S3StorageException>(block = {
            storage.putBucketPolicy(bucketName) {
                statement.add(bigTestStatement)
            }
        })

        assertNotEquals(
            bigTestStatement.resource,
            policyStatement?.resource
        )

        assertFailsWith<S3StorageException>(block = {
            storage.buildStatement {
                effect = EffectEnum.ALLOW
                resource.add(storage.buildResource {})
                principal = storage.buildPrincipal {
                    aws.add(PrincipalEnum.PUBLIC_ACCESS.accessName)
                }
            }
        })

        assertFailsWith<S3StorageException>(block = {
            storage.buildStatement {
                effect = EffectEnum.ALLOW
                resource.add(storage.buildResource {})
                action.add(ActionEnum.GET_OBJECT)
            }
        })

        assertFailsWith<S3StorageException>(block = {
            storage.buildStatement {
                effect = EffectEnum.ALLOW
                action.add(ActionEnum.GET_OBJECT)
                principal = storage.buildPrincipal {
                    aws.add(PrincipalEnum.PUBLIC_ACCESS.accessName)
                }
            }
        })

        assertFailsWith<S3StorageException>(block = {
            storage.buildStatement {
                effect = EffectEnum.ALLOW
                action.add(ActionEnum.GET_OBJECT)
                resource.add(storage.buildResource { })
                principal = storage.buildPrincipal {
                }
            }
        })

        val deletePolicyResult = storage.deleteBucketPolicy(bucketName)
        assertTrue(deletePolicyResult)

        val policyAfterDelete = storage.getBucketPolicy(bucketName)
        assertNull(policyAfterDelete)
    }

    @After
    fun close() {
        s3.close()
    }

    private fun getFile(fileType: FileType): Pair<String, InputStream> {
        val key = storage.generateFileKey()
        val fileName = when (fileType) {
            FileType.JPG -> dotenv["JPG_TEST_OBJECT"] ?: throw Exception("JPG File not found")
            FileType.GIF -> dotenv["GIF_TEST_OBJECT"] ?: throw Exception("GIF File not found")
            FileType.PNG -> dotenv["PNG_TEST_OBJECT"] ?: throw Exception("PNG File not found")
            FileType.PDF -> dotenv["PDF_TEST_OBJECT"] ?: throw Exception("PDF File not found")
            FileType.ZIP -> dotenv["ZIP_TEST_OBJECT"] ?: throw Exception("ZIP File not found")
            FileType.BIN -> dotenv["BIN_TEST_OBJECT"] ?: throw Exception("BIN File not found")
        }

        return key to (classLoader.getResourceAsStream(fileName) ?: throw Exception("File not readable"))
    }

    private enum class FileType {
        JPG,
        GIF,
        PNG,
        PDF,
        ZIP,
        BIN;
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
                for (i in 0 until min(n1, n2)) if (buf1.get() != buf2.get()) return false
                buf1.compact()
                buf2.compact()
            }
        } finally {
            i1.close()
            i2.close()
        }
    }

    companion object {
        private lateinit var s3: S3Client
        private lateinit var preSigner: S3Presigner
        private lateinit var storage: IS3Storage
    }
}
