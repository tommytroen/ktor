/*
 * Copyright 2014-2020 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.tests.utils

import io.ktor.client.features.logging.*
import io.ktor.util.collections.*
import kotlin.test.*

internal class TestLogger(private vararg val expectedLog: String) : Logger {
    private val log = ConcurrentList<String>()

    override fun log(message: String) {
        log += message
    }

    fun reset() {
        log.clear()
    }

    fun verify() {
        var expectedIndex = 0
        var actualIndex = 0

        while (expectedIndex < expectedLog.size && actualIndex < log.size) {
            var expected = expectedLog[expectedIndex].toLowerCase()
            val actual = log[actualIndex].toLowerCase()

            var flaky = false
            var optional = false

            if (expected.startsWith("!!!")) {
                expected = expected.substring(3)
                flaky = true
            }

            if (expected.startsWith("???")) {
                expected = expected.substring(3)
                optional = true
            }

            if (expected == actual || flaky) {
                expectedIndex++
                actualIndex++
                continue
            }

            if (optional) {
                expectedIndex++
                continue
            }

            if (expected != actual) {
                println(">>> Expected log:")
                expectedLog.forEach {
                    println(it)
                }

                println(">>> Actual log:")
                log.forEach {
                    println(it)
                }

                fail("Expected log doesn't match actual at lines: expected $expectedIndex, actual $actualIndex")
            }
        }

        val message = StringBuilder()
        if (actualIndex < log.size) {
            message.append("Actual log was not fully processed:\n")
            message.appendLog(log.subList(actualIndex, log.size))
        }

        if (expectedIndex < expectedLog.size) {
            message.append("Expected log was not fully processed:\n")
            message.appendLog(expectedLog.asList().subList(expectedIndex, expectedLog.size))
        }

        if (message.isNotEmpty()) {
            error(message)
        }
    }
}

private fun StringBuilder.appendLog(log: List<String>) {
    for (line in log) {
        append('"')
        append(log)
        append('"')
        append(',')
        append('\n')
    }
}

internal class CustomError(override val message: String) : Throwable() {
    override fun toString(): String = "CustomError[$message]"
}
