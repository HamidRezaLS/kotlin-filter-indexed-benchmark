# Kotlin Sequence.filterIndexed Benchmark

A standalone Kotlin Multiplatform benchmark comparing the current 3-wrapper sequence implementation against an optimized single-layer iterator for `Sequence.filterIndexed`.

Created for YouTrack issue [KT-89925](https://youtrack.jetbrains.com/issue/KT-89925) and Kotlin PR [JetBrains/kotlin#8662](https://github.com/JetBrains/kotlin/pull/8662).

## How to Run

```bash
# JVM (standard timing)
./gradlew jvmBenchmark

# JVM with JMH GC allocation profiler
./gradlew jvmGcBenchmark

# WebAssembly (Node.js)
./gradlew wasmJsBenchmark

# Linux Native (x64)
./gradlew linuxX64Benchmark

# macOS Apple Silicon (run on macOS)
./gradlew macosArm64Benchmark
```

## Results Summary

Tested on AMD Ryzen 7 6800HS (Linux x86_64, Node.js 22, OpenJDK 21):

### Iteration Speedup (`for (x in seq)`)

| Target | 100 elements | 10,000 elements | 100,000 elements |
| :--- | :---: | :---: | :---: |
| **Linux Native (`linuxX64`)** | 2.44x faster | 2.10x faster | 2.43x faster |
| **WebAssembly (`wasmJs`)** | 2.08x faster | 1.97x faster | 2.09x faster |
| **JVM (`jvm`)** | 1.68x faster | 1.50x faster | 1.92x faster |

### Memory & Allocations (JVM JMH -prof gc)

| Dataset Size | Baseline Alloc | Optimized Alloc | Reduction |
| :--- | :---: | :---: | :---: |
| **100 elements** | 2,400 B/op | ~0 B/op | -100% (zero allocations) |
| **10,000 elements** | 398 KB/op | 158 KB/op | -60.3% |
| **100,000 elements** | 4.0 MB/op | 1.6 MB/op | -60.0% |

*(Materializing via `toList()` also cuts heap usage roughly in half, from 4.85 MB down to 2.45 MB at 100k items).*
