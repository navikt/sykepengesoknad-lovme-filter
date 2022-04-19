import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.6.6"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.spring") version "1.6.21"
}

group = "no.nav.helse.flex"
version = "1.0.0"
description = "sykepengesoknad-lovme-filter"

val githubUser: String by project
val githubPassword: String by project

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/navikt/maven-release")
    }

    maven {
        url = uri("https://maven.pkg.github.com/navikt/sykepengesoknad-kafka")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
}

val testContainersVersion = "1.16.3"
val logstashLogbackEncoderVersion = "7.0.1"
val mockitoKotlinVersion = "2.2.0"
val kluentVersion = "1.68"
val sykepengesoknadKafkaVersion = "2022.02.10-16.07-0892e94a"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.slf4j:slf4j-api")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashLogbackEncoderVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("no.nav.helse.flex:sykepengesoknad-kafka:$sykepengesoknadKafkaVersion")
    implementation("io.micrometer:micrometer-registry-prometheus")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:kafka:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("org.awaitility:awaitility")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("app.jar")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
        kotlinOptions.allWarningsAsErrors = true
    }
}
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("PASSED", "FAILED", "SKIPPED")
    }
}
