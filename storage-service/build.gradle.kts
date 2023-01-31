/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */
import java.util.Base64

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("kotlin-kapt")
    id("maven-publish")
    id("java-library")
    id("signing")
}

apply(plugin = "java")
apply(plugin = "kotlin")

group = "com.icerockdev"
version = "0.7.0"

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

dependencies {
    // AWS S3 SDK
    api("software.amazon.awssdk:s3:${properties["aws_sdk_s3_version"]}")
    // Apache Tika
    implementation("org.apache.tika:tika-core:${properties["apache_tika_version"]}")
    // Logging
    implementation("ch.qos.logback:logback-classic:${properties["logback_version"]}")
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${properties["coroutines_version"]}")
    // Image processing
    implementation("com.sksamuel.scrimage:scrimage-core:${properties["scrimage_version"]}")
    // Tests
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${properties["kotlin_version"]}")
    // Load configuration in tests
    testImplementation("io.github.cdimascio:java-dotenv:${properties["java_dotenv_version"]}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${properties["jackson_version"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withJavadocJar()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

repositories {
    mavenCentral()
}

publishing {
    repositories.maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
        name = "OSSRH"

        credentials {
            username = System.getenv("OSSRH_USER")
            password = System.getenv("OSSRH_KEY")
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
            pom {
                name.set("Storage service")
                description.set("Tools for work with s3 storage and generate preview")
                url.set("https://github.com/icerockdev/storage-service")
                licenses {
                    license {
                        url.set("https://github.com/icerockdev/storage-service/blob/master/LICENSE.md")
                    }
                }

                developers {
                    developer {
                        id.set("YokiToki")
                        name.set("Stanislav")
                        email.set("skarakovski@icerockdev.com")
                    }

                    developer {
                        id.set("AlexeiiShvedov")
                        name.set("Alex Shvedov")
                        email.set("ashvedov@icerockdev.com")
                    }

                    developer {
                        id.set("oyakovlev")
                        name.set("Oleg Yakovlev")
                        email.set("oyakovlev@icerockdev.com")
                    }
                }

                scm {
                    connection.set("scm:git:ssh://github.com/icerockdev/storage-service.git")
                    developerConnection.set("scm:git:ssh://github.com/icerockdev/storage-service.git")
                    url.set("https://github.com/icerockdev/storage-service")
                }

                organization {
                    name.set("IceRock Development")
                    url.set("https://icerockdev.com")
                }
            }
        }

        val withoutSigning: Boolean = project.gradle.startParameter.taskNames.contains("publishToMavenLocal")
        if (!withoutSigning) {
            signing {
                val signingKeyId: String? = System.getenv("SIGNING_KEY_ID")
                val signingPassword: String? = System.getenv("SIGNING_PASSWORD")
                val signingKey: String? = System.getenv("SIGNING_KEY")?.let { base64Key ->
                    String(Base64.getDecoder().decode(base64Key))
                }
                useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
                sign(publishing.publications["mavenJava"])
            }
        }
    }
}
