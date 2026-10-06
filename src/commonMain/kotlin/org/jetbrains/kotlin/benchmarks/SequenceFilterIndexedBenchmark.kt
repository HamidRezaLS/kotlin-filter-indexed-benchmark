/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.benchmarks

import kotlinx.benchmark.*

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MICROSECONDS)
open class SequenceFilterIndexedBenchmark {

    @Param("100", "10000", "100000")
    var size: Int = 0

    private lateinit var data: List<Int>

    @Setup
    fun setUp() {
        data = (0 until size).toList()
    }

    // Baseline: simulates the legacy 3-layer iterator sequence implementation:
    // TransformingSequence(FilteringSequence(IndexingSequence(this), ...))
    private fun <T> Sequence<T>.filterIndexedBaseline(predicate: (index: Int, T) -> Boolean): Sequence<T> {
        return this.withIndex().filter { predicate(it.index, it.value) }.map { it.value }
    }

    // Optimized: dedicated single-layer sequence iterator maintaining an internal primitive index counter
    private fun <T> Sequence<T>.filterIndexedOptimized(predicate: (index: Int, T) -> Boolean): Sequence<T> {
        return FilteringIndexedSequence(this, true, predicate)
    }

    @Benchmark
    fun baselineConsume(bh: Blackhole) {
        for (item in data.asSequence().filterIndexedBaseline { index, _ -> index % 2 == 0 }) {
            bh.consume(item)
        }
    }

    @Benchmark
    fun optimizedConsume(bh: Blackhole) {
        for (item in data.asSequence().filterIndexedOptimized { index, _ -> index % 2 == 0 }) {
            bh.consume(item)
        }
    }

    @Benchmark
    fun baselineToList(bh: Blackhole) {
        val result = data.asSequence().filterIndexedBaseline { index, _ -> index % 2 == 0 }.toList()
        bh.consume(result)
    }

    @Benchmark
    fun optimizedToList(bh: Blackhole) {
        val result = data.asSequence().filterIndexedOptimized { index, _ -> index % 2 == 0 }.toList()
        bh.consume(result)
    }
}
