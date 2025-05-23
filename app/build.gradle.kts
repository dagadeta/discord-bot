buildscript {
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:${libs.versions.flyway.plugin.get()}")
    }
}

plugins {
    alias(libs.plugins.flyway)
    alias(libs.plugins.jacocolog)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    application
}

group = "de.dagadeta"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.slf4j:slf4j-api")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation(libs.jda)
    implementation(libs.json)
    implementation(libs.kotlin.logging)
    implementation(libs.okhttp)
    runtimeOnly("org.postgresql:postgresql")

    developmentOnly("org.flywaydb:flyway-database-postgresql")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    testImplementation("org.assertj:assertj-core")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.zonky)
    testRuntimeOnly("org.flywaydb:flyway-core")
    testRuntimeOnly("org.flywaydb:flyway-database-postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.postgresql:postgresql")
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
