import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "fun.nymudpee"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

val lwjglVersion = "3.3.3"
val ktorVersion = "2.3.8"

val lwjglNatives = Pair(
    System.getProperty("os.name")!!,
    System.getProperty("os.arch")!!
).let { (name, arch) ->
    when {
        arrayOf("Linux", "SunOS", "Unit").any { name.startsWith(it) } ->
            if (arrayOf("arm", "aarch64").any { arch.startsWith(it) })
                "natives-linux${if (arch.contains("64") || arch.startsWith("armv8")) "-arm64" else "-arm32"}"
            else if (arch.startsWith("ppc"))
                "natives-linux-ppc64le"
            else if (arch.startsWith("riscv"))
                "natives-linux-riscv64"
            else
                "natives-linux"

        arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) } ->
            "natives-macos${if (arch.startsWith("aarch64")) "-arm64" else ""}"

        arrayOf("Windows").any { name.startsWith(it) } ->
            if (arch.contains("64"))
                "natives-windows${if (arch.startsWith("aarch64")) "-arm64" else ""}"
            else
                "natives-windows-x86"

        else ->
            throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
    }
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    // https://mvnrepository.com/artifact/org.lwjgl/lwjgl-stb
    implementation("org.lwjgl:lwjgl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-stb:$lwjglVersion")
    runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-stb:$lwjglVersion:$lwjglNatives")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    // https://mvnrepository.com/artifact/org.apache.commons/commons-imaging
    implementation("org.apache.commons:commons-imaging:1.0.0-alpha4")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "image4cd"
            packageVersion = "1.0.0"
        }
    }
}
