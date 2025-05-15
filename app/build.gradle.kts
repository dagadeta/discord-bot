buildscript {
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:${libs.versions.flyway.plugin.get()}")
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm)
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "1.9.25"
    alias(libs.plugins.flyway)

    application
}

group = "de.dagadeta"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jda)
    implementation(libs.logback.classic)
    implementation(libs.slf4j.api)
    implementation(libs.kotlin.logging)
    implementation(libs.okhttp)
    implementation(libs.json)
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("org.postgresql:postgresql")

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    developmentOnly("org.flywaydb:flyway-database-postgresql")

    testImplementation(libs.assertj)
    testImplementation(libs.zonky)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.postgresql:postgresql")
    testRuntimeOnly("org.flywaydb:flyway-core")
    testRuntimeOnly("org.flywaydb:flyway-database-postgresql")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

application {
    mainClass = "de.dagadeta.schlauerbot.AppKt"
}

flyway {
    url = "jdbc:postgresql://localhost:5432/discordbot"
    user = "pguser"
    password = "pguser"
    schemas = arrayOf("wordchaingame")
    baselineOnMigrate = true
}

tasks {
    named<Test>("test") {
        useJUnitPlatform()
    }

    bootDistZip {
        dependsOn(check)
    }
}
