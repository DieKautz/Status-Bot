import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("com.github.johnrengelman.shadow") version "5.1.0"
}

group = "dev.diekautz"
version = "fca00c-2.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.20.0")
    implementation("org.tinylog:slf4j-tinylog:2.6.0")
    implementation("org.tinylog:tinylog-impl:2.6.0")
    implementation("org.tinylog:tinylog-api-kotlin:2.6.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    implementation("io.ktor:ktor-client-core-jvm:2.2.4")
    runtimeOnly("io.ktor:ktor-client-apache-jvm:2.2.4")
    implementation("org.javacord:javacord:3.7.0")
}

tasks.withType<ShadowJar> {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
}