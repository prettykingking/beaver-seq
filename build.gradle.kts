import org.gradle.api.internal.plugins.DefaultTemplateBasedStartScriptGenerator

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "org.jiezheng"
version = "0.1.0"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

distributions {
    main {
        distributionBaseName = "BeaverSeq"
        contents {
            into("bin") {
                from("scripts")
                exclude("unixStartScript.sh", "windowsStartScript.bat")
            }
            into("logs") {
            }
            into("resources") {
                from("src/main/resources")
            }
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.metrics)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation("com.mysql:mysql-connector-j:9.1.0") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
    implementation("com.zaxxer:HikariCP:6.1.0")
    implementation(libs.exposed.dao)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}

tasks.getByName<CreateStartScripts>("startScripts") {
    val unixGenerator = unixStartScriptGenerator as DefaultTemplateBasedStartScriptGenerator
    unixGenerator.template = resources.text.fromFile("scripts/unixStartScript.sh")
    val windowsGenerator = windowsStartScriptGenerator as DefaultTemplateBasedStartScriptGenerator
    windowsGenerator.template = resources.text.fromFile("scripts/windowsStartScript.bat")
}

tasks.getByName<ProcessResources>("processResources") {
    exclude("application.conf", "logback.xml", "sequence.sql")
}

tasks.getByName<Jar>("jar") {
}
