import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.io.ktor.plugin)
}

group = "io.eqoty"
version = "0.0.1"

kotlin {
    jvm {
        withJava()
    }
    val nativeTargets = listOf<KotlinNativeTarget>(
        macosArm64(),
        macosX64(),
        linuxX64(),
        linuxArm64(),
        mingwX64(),
    )
    for (target in nativeTargets) {
        target.apply {
            binaries {
                executable {
                    entryPoint = "io.eqoty.server.main"
                }
            }
        }
    }
    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.server.host.common)
                implementation(libs.ktor.server.cio)
                implementation(libs.io.core)
                implementation(libs.io.bytestring)
            }
        }
        jvmMain {
            dependencies {
                implementation(libs.logback.classic)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.ktor.server.tests)
            }
        }
    }
}

application {
    mainClass.set("io.eqoty.server.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}