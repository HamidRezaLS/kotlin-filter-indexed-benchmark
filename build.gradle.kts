import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "2.2.0"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.17"
    id("org.jetbrains.kotlin.plugin.allopen") version "2.2.0"
}

allOpen {
    annotation("kotlinx.benchmark.State")
}

val isMac = System.getProperty("os.name").startsWith("Mac", ignoreCase = true)

kotlin {
    jvmToolchain(21)

    jvm()
    linuxX64()
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
    }
    if (isMac) {
        macosArm64()
    }

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.17")
        }
    }
}

benchmark {
    targets {
        register("jvm")
        register("linuxX64")
        register("wasmJs")
        if (isMac) {
            register("macosArm64")
        }
    }

    configurations {
        named("main") {
            iterationTime = 1
            iterationTimeUnit = "sec"
            warmups = 3
            iterations = 5
            outputTimeUnit = "us"
        }
    }
}

afterEvaluate {
    tasks.register<JavaExec>("jvmGcBenchmark") {
        dependsOn("jvmBenchmarkCompile")
        val jvmBenchmark = tasks.named<JavaExec>("jvmBenchmark").get()
        classpath(jvmBenchmark.classpath)
        javaLauncher.set(jvmBenchmark.javaLauncher)
        mainClass.set("org.openjdk.jmh.Main")
        args(
            ".*SequenceFilterIndexedBenchmark.*",
            "-prof", "gc",
            "-wi", "2",
            "-i", "3",
            "-f", "1",
            "-tu", "us",
            "-bm", "avgt"
        )
    }
}
