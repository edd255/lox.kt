import org.gradle.jvm.toolchain.JvmVendorSpec

plugins {
    alias(libs.plugins.jvm)
    application
    alias(libs.plugins.graalvm.native)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)

    testRuntimeOnly(libs.junit.platform.launcher)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "dev.edd255.lox.MainKt"
}

graalvmNative {
    toolchainDetection.set(true)
    binaries {
        named("main") {
            // The selected GraalVM installation must include the native-image executable.
            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(21))
                vendor.set(JvmVendorSpec.GRAAL_VM)
            })
            imageName.set("lox")
        }
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    outputs.upToDateWhen { false }
    testLogging {
        events("failed", "standardOut", "standardError")
    }
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
    standardInput = System.`in`
}
