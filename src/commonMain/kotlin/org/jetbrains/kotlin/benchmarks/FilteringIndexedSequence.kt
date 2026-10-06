/*
 * Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.benchmarks

internal fun checkIndexOverflow(index: Int): Int {
    if (index < 0) {
        throw ArithmeticException("Index overflow has happened")
    }
    return index
}

/**
 * A sequence that returns the values from the underlying [sequence] that either match or do not match
 * the specified [predicate], where the predicate function takes the index of the value in the underlying
 * sequence along with the value itself.
 */
internal class FilteringIndexedSequence<T>(
    private val sequence: Sequence<T>,
    private val sendWhen: Boolean = true,
    private val predicate: (index: Int, T) -> Boolean
) : Sequence<T> {

    override fun iterator(): Iterator<T> = object : Iterator<T> {
        val iterator = sequence.iterator()
        var index = 0
        var nextState: Int = -1 // -1 for unknown, 0 for done, 1 for continue
        var nextItem: T? = null

        private fun calcNext() {
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (predicate(checkIndexOverflow(index++), item) == sendWhen) {
                    nextItem = item
                    nextState = 1
                    return
                }
            }
            nextState = 0
        }

        override fun next(): T {
            if (nextState == -1)
                calcNext()
            if (nextState == 0)
                throw NoSuchElementException()
            val result = nextItem
            nextItem = null
            nextState = -1
            @Suppress("UNCHECKED_CAST")
            return result as T
        }

        override fun hasNext(): Boolean {
            if (nextState == -1)
                calcNext()
            return nextState == 1
        }
    }
}
