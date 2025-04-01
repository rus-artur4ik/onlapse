/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.jetpackcamera.core.common

import kotlinx.coroutines.delay

suspend fun <T> retryWithBackoff(
    times: Int = 4,
    initialDelay: Long = 1000,
    maxDelay: Long = 16000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    require(times > 0) { "Try count (times) must be greater than 0." }

    var currentDelay = initialDelay
    var attempt = 0

    repeat(times - 1) {
        try {
            return block()
        } catch (e: Exception) {
            attempt++
            println("Try #$attempt failed: ${e.message}. " +
                    "Retry after $currentDelay ms.")
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }

    // Последняя попытка (если предыдущие не удались)
    return block()
}