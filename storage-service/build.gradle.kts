/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("kotlin-kapt")
    id("maven-publish")
    id("java-library")
}

apply(plugin = "java")
apply(plugin = "kotlin")

group = "com.icerockdev"
version = "0.1.0"

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${properties["kotlin_version"]}")
    // min.IO
    api("io.minio:minio:${properties["minio_version"]}")
    // Ktor
    api("io.ktor:ktor-server-core:${properties["ktor_version"]}")
    // Logging
    implementation("ch.qos.logback:logback-classic:${properties["logback_version"]}")
    // Imagick
    implementation("jmagick:jmagick:${properties["jmagick_version"]}")
    // Date/Time
    implementation("joda-time:joda-time:${properties["jodatime_version"]}")
    // Tests
    testImplementation("junit:junit:${properties["junit_version"]}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

repositories {
    mavenCentral()
}

publishing {
    repositories.maven("https://api.bintray.com/maven/icerockdev/backend/storage-service/;publish=1") {
        name = "bintray"

        credentials {
            username = System.getProperty("BINTRAY_USER")
            password = System.getProperty("BINTRAY_KEY")
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}
