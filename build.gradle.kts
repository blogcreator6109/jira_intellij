plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.16.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
}

kotlin {
    jvmToolchain(17)
}

intellij {
    version.set("2023.2")
    type.set("IC")
    plugins.set(listOf("git4idea"))
}

tasks {
    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("")
    }
    buildSearchableOptions {
        enabled = false
    }
}
