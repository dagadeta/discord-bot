buildscript {
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:${libs.versions.flyway.plugin.get()}")
    }
}

plugins {
    alias(libs.plugins.flyway)
    alias(libs.plugins.kover)
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

testing {
    suites {
        register<JvmTestSuite>("integTest") {
            dependencies { implementation(sourceSets.main.get().output) }
        }
        register<JvmTestSuite>("e2eTest") {
            dependencies { implementation(sourceSets.main.get().output) }
        }
    }
}

val integTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
val integTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}
val e2eTestImplementation: Configuration by configurations.getting {
    extendsFrom(integTestImplementation)
}
val e2eTestRuntimeOnly: Configuration by configurations.getting {
    extendsFrom(integTestRuntimeOnly)
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation(libs.jda)
    implementation(libs.json)
    implementation(libs.kotlin.logging)
    implementation(libs.okhttp)
    runtimeOnly("org.postgresql:postgresql")

    developmentOnly("org.flywaydb:flyway-database-postgresql")
    developmentOnly("org.springframework.boot:spring-boot-starter-flyway")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation(libs.mockito.kotlin)

    integTestImplementation("org.springframework.boot:spring-boot-starter-test")
    integTestImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    integTestImplementation(libs.zonky)
    integTestRuntimeOnly("org.junit.platform:junit-platform-launcher")
    integTestRuntimeOnly("org.flywaydb:flyway-database-postgresql")
    integTestRuntimeOnly("org.springframework.boot:spring-boot-starter-flyway")
    integTestRuntimeOnly("org.postgresql:postgresql")
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
    schemas = arrayOf("wordchaingame", "discordbot")
    baselineOnMigrate = true
}

tasks {
    named<Test>("test") {
        useJUnitPlatform()
    }

    bootDistZip {
        dependsOn("test", "integTest")
    }
}
