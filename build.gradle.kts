plugins {
    kotlin("js") version "1.7.20"
    kotlin("plugin.serialization") version "1.7.20"
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
    id("io.github.turansky.kfc.legacy-union") version "5.8.0"
}

group = "com.sdercolin.harmoloid"

repositories {
    mavenCentral()
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("0.45.2")
    enableExperimentalRules.set(true)
}

fun kotlinw(target: String): String =
    "org.jetbrains.kotlin-wrappers:kotlin-$target"

val kotlinWrappersVersion = "1.0.0-pre.354"

dependencies {
    // Core
    implementation("com.sdercolin.harmoloid:harmoloid-core:1.3")

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

    // React, React DOM + Wrappers
    implementation(enforcedPlatform(kotlinw("wrappers-bom:$kotlinWrappersVersion")))
    implementation(kotlinw("emotion"))
    implementation(kotlinw("react"))
    implementation(kotlinw("react-dom"))
    implementation(kotlinw("mui"))
    implementation(kotlinw("mui-icons"))
    implementation(kotlinw("react-css:18.0.0-pre.331-kotlin-1.6.20"))

    // React components
    implementation(npm("react-file-drop", "3.1.2"))
    implementation(npm("react-markdown", "5.0.3"))

    // Localization
    implementation(npm("i18next", "19.8.7"))
    implementation(npm("react-i18next", "11.8.5"))
    implementation(npm("i18next-browser-languagedetector", "6.0.1"))

    // Others
    implementation(npm("jszip", "3.5.0"))
    implementation(npm("stream-browserify", "3.0.0"))
    implementation(npm("buffer", "6.0.3"))
    implementation(npm("file-saver", "2.0.5"))
    implementation(npm("raw-loader", "4.0.2"))
    implementation(npm("file-loader", "6.2.0"))
    implementation(npm("encoding-japanese", "1.0.30"))
    implementation(npm("uuid", "8.3.2"))
    implementation(npm("midi-parser-js", "4.0.4"))
    implementation(npm("js-cookie", "2.2.1"))
}

kotlin {
    js(LEGACY) {
        browser {
            binaries.executable()
            commonWebpackConfig {
                cssSupport.enabled = true
            }
            runTask {
                cssSupport.enabled = true
            }
            testTask {
                useKarma {
                    enabled = true
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
    }
}
