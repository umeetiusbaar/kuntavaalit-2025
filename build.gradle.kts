val ktor_version: String by project
val jackson_version: String by project
val ksoup_version: String by project
val kotlin_logging_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "2.1.10"
}

group = "fi.lempimetsa"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-gson:$ktor_version")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jackson_version")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version")
    implementation("com.mohamedrejeb.ksoup:ksoup-html:$ksoup_version")

    implementation("io.github.oshai:kotlin-logging-jvm:$kotlin_logging_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
