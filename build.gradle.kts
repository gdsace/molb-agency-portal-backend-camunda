import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.Date

group = "sg.gov.tech"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    // jcenter()
    maven { url = uri("https://repo.spring.io/milestone") }

    maven {
        url =
            uri("https://${System.getenv()["G2B_NEXUS_HOST"] ?: "nexus.ship.gov.sg"}/repository/gobiz-maven-public/")
        credentials {
            username = System.getenv()["G2B_NEXUS_USERNAME"]
            password = System.getenv()["G2B_NEXUS_PASSWORD"]
        }
    }
}

buildscript {
    repositories {
        mavenCentral()
        // jcenter()
    }
}

java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11

plugins {
    idea
    java
    jacoco

    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"

    id("org.springframework.boot") version "2.7.8"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    id("io.gitlab.arturbosch.detekt") version "1.22.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
    id("org.flywaydb.flyway") version "8.5.10"
    id("org.jetbrains.kotlin.plugin.jpa") version "1.6.21"
}

extra["springCloudVersion"] = "2021.0.2"
extra["snakeyaml.version"] = "1.33"
extra["netty.version"] = "4.1.86.Final"
extra["kotlin.version"] = "1.7.22"

// Forces IntelliJ to use same output folder as Gradle ('build' folder, not 'out' or 'target')
idea {
    project {
        jdkName = "11"
    }
    module {
        outputDir = file("$buildDir/classes/main")
        testOutputDir = file("$buildDir/classes/test")
        isDownloadSources = true
    }
}

jacoco {
    toolVersion = "0.8.8"
}

dependencies {
    // Spring
    implementation("org.hibernate.validator:hibernate-validator:6.0.22.Final")
    implementation("org.hibernate:hibernate-core:5.6.11.Final")
    implementation("org.hibernate:hibernate-envers:5.6.11.Final")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:3.1.3")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner")
    testImplementation("org.springframework.security:spring-security-test")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.security:spring-security-oauth2-resource-server")
    implementation("com.nimbusds:oauth2-oidc-sdk:9.43.1")

    //	Camunda Dependencies
    implementation ("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-rest:7.17.0")
    implementation ("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-webapp:7.17.0")
    implementation ("org.camunda.bpm:camunda-engine-plugin-spin:7.17.0")
    implementation ("org.camunda.spin:camunda-spin-core:1.16.0")
    implementation ("org.camunda.spin:camunda-spin-dataformat-json-jackson:1.16.0")


    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // DB
    implementation("org.flywaydb:flyway-core")
    implementation("com.vladmihalcea:hibernate-types-52:2.16.2")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.testcontainers:postgresql:1.15.1")

    // Open API
    implementation("org.springdoc:springdoc-openapi-ui:1.6.8")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.6.8")

    // JWT and Cryptography
    implementation("com.nimbusds:nimbus-jose-jwt:9.15.2")
    implementation("io.jsonwebtoken:jjwt:0.9.1")

    // G2B commons
    implementation("sg.gov.tech.molb:molb-backend-common:0.1.106")

    // Logging
    implementation("net.logstash.logback:logstash-logback-encoder:7.1.1")

    // Others
    implementation("com.google.guava:guava:31.1-jre")
    implementation(platform("com.amazonaws:aws-java-sdk-bom:1.12.297"))
    implementation("com.amazonaws:aws-java-sdk-sqs")

    implementation("commons-io:commons-io:2.7")

    // Testing
    testImplementation("io.mockk:mockk:1.12.4")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.33.2")
    testImplementation("com.tngtech.archunit:archunit-junit5:0.23.1")
    testImplementation("com.tngtech.archunit:archunit-junit5-api:0.23.1")
    testImplementation("com.tngtech.archunit:archunit-junit5-engine:0.23.1")
    testImplementation("net.java.dev.jna:jna-platform:5.12.1")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

detekt {
    config = files("${rootProject.projectDir}/config/detekt/detekt.yml")
    baseline = file("${rootProject.projectDir}/config/detekt/baseline.xml")
}

// To exclude classes or functions from the coverage test, tag the class/function with
// @ExcludeFromGeneratedCoverageTest (sg.gov.tech.molbagencyportalbackend.annotation)
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = "CLASS"
            excludes = listOf()

            limit {
                minimum = "0.7".toBigDecimal()
            }
        }
    }
}

// Running this task alone will not yield the report you want! You need to run the tests first, so
// that the results are in the "build" folder, so that this task can parse those results into the
// HTML report.
tasks.jacocoTestReport {
    reports {
        xml.required.set(false)
        csv.required.set(false)
        html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
    }
}

val archUnitSourceSet = sourceSets.create("archUnitSourceSet") {
    withConvention(org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet::class) {
        kotlin.srcDir("src/test/kotlin/sg/gov/tech/molbagencyportalbackend")
    }

    val testSourceSet = sourceSets.test.get()

    compileClasspath += testSourceSet.runtimeClasspath
    runtimeClasspath += testSourceSet.runtimeClasspath
}

val runArchitectureTests = tasks.register<Test>("runArchitectureTests") {
    description = "Runs architecture tests only."
    group = "verification"
    testClassesDirs = archUnitSourceSet.output.classesDirs
    classpath = archUnitSourceSet.runtimeClasspath
    mustRunAfter(tasks["test"])
}

val runUnitTests = tasks.register<Test>("runUnitTests") {
    description = "Runs unit tests only."
    group = "verification"
    useJUnitPlatform {
        includeTags("unit-test")
    }
}

val runIntegrationTests = tasks.register<Test>("runIntegrationTests") {
    description = "Runs integration tests only."
    group = "verification"
    useJUnitPlatform {
        includeTags("integration")
    }
}

flyway {
    val databaseHost = project.findProperty("dbHost") ?: "localhost"
    val databaseName = project.findProperty("dbName") ?: "molbap"
    val databaseUsername = project.findProperty("dbUsername") ?: "test"
    val databasePassword = project.findProperty("dbPassword") ?: "test"
    url =
        "jdbc:postgresql://$databaseHost:5438/$databaseName?user=$databaseUsername&password=$databasePassword"
    locations = arrayOf("classpath:db/migration")
}

tasks.register("flywayNewMigration") {
    group = "flyway"
    description =
        "Create new migration script by running ./gradlew flywayNewMigration -PmigrationName=<migration-name>"
    doLast {
        val timestamp = SimpleDateFormat("yyyyMMddHHmm").format(Date())
        val migrationName: String by project
        val file =
            "${project.sourceSets.main.get().resources.sourceDirectories.singleFile}/db/migration/V${timestamp}__$migrationName.sql"
        File(file).createNewFile()
    }
}
