plugins {
    kotlin("js") version "1.6.0"
    kotlin("plugin.serialization") version "1.5.31"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
}

group = "com.sdercolin.harmoloid"

repositories {
    mavenCentral()
    jcenter()
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("0.36.0")
}

dependencies {
    // Core
    implementation("com.sdercolin.harmoloid:harmoloid-core:1.2")

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")

    // React, React DOM + Wrappers
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.264-kotlin-1.5.31")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.264-kotlin-1.5.31")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-styled:5.3.3-pre.264-kotlin-1.5.31")
    implementation(npm("react", "17.0.2"))
    implementation(npm("react-dom", "17.0.2"))

    // React components
    implementation(npm("@material-ui/core", "4.11.4"))
    implementation(npm("@material-ui/icons", "4.11.2"))
    implementation(npm("@material-ui/lab", "4.0.0-alpha.58"))
    implementation(npm("react-file-drop", "3.1.2"))
    implementation(npm("react-is", "17.0.2"))
    implementation(npm("react-markdown", "5.0.3"))

    // Localization
    implementation(npm("i18next", "19.8.7"))
    implementation(npm("react-i18next", "11.8.5"))
    implementation(npm("i18next-browser-languagedetector", "6.0.1"))

    // Others
    implementation(npm("stream", "0.0.2"))
    implementation(npm("jszip", "3.5.0"))
    implementation(npm("file-saver", "2.0.5"))
    implementation(npm("raw-loader", "4.0.2"))
    implementation(npm("file-loader", "6.2.0"))
    implementation(npm("encoding-japanese", "1.0.30"))
    implementation(npm("uuid", "8.3.2"))
    implementation(npm("midi-parser-js", "4.0.4"))
    implementation(npm("js-cookie", "2.2.1"))
}

kotlin {
    js {
        browser {
            binaries.executable()
            webpackTask {
                cssSupport.enabled = true
            }
            runTask {
                cssSupport.enabled = true
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
    }
}

tasks.register("stage") {
    dependsOn("build")
}
