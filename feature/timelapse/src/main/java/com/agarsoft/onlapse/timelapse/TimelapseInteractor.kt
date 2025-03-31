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

package com.agarsoft.onlapse.timelapse

import android.app.Application
import android.widget.Toast
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

class TimelapseInteractor @Inject constructor(
    val app: Application
) {

    private val state = TimelapseCommands.timelapseMutableState
    private val commands = TimelapseCommands.mutableCommands

    fun toggleTimelapse(framesPerDay: Int) {
        when (state.value) {
            is TimelapseInternalState.Idle -> {
                startTimelapse(framesPerDay)
            }
            is TimelapseInternalState.Capturing -> {
                stopTimelapse()
            }
        }

    }

    fun startTimelapse(
        framesPerDay: Int
    ) {
        if (state.value is TimelapseInternalState.Capturing) {
            throw IllegalStateException("Timelapse already started")
        }

        state.value = TimelapseInternalState.Capturing(
            framesCaptured = 0,
            startTime = Instant.now(),
        )

        val repeatIntervalHours = 24.0 / framesPerDay
        val uploadWorkRequest = PeriodicWorkRequestBuilder<TimelapseWorker>(
            repeatInterval = repeatIntervalHours.hours.toJavaDuration()
        )
            .addTag(TimelapseWorker.TAG)
            .build()

        WorkManager
            .getInstance(app)
            .enqueue(uploadWorkRequest)



        Toast.makeText(
            app,
            "Timelapse started with $framesPerDay frames per day",
            Toast.LENGTH_LONG
        ).show()
    }

    fun stopTimelapse() {
        WorkManager
            .getInstance(app)
            .cancelAllWorkByTag(TimelapseWorker.TAG)
        state.value = TimelapseInternalState.Idle
    }
}