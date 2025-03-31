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
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.agarsoft.onlapse.timelapse.TimelapseInternalState.Idle
import com.google.jetpackcamera.core.camera.TimelapseRecordingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import java.time.Instant

object TimelapseCommands {

    private val appFlow = MutableStateFlow<Application?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val scheduledInstantsFlow = appFlow
        .filterNotNull()
        .flatMapLatest { app ->
            WorkManager
                .getInstance(app)
                .getWorkInfosByTagFlow(TimelapseWorker.TAG)
                .map { list ->
                    list
                        .filter {
                            it.state == WorkInfo.State.ENQUEUED
                        }
                        .map {
                            Instant.ofEpochMilli(
                                it.nextScheduleTimeMillis
                            )
                        }
                }
                .onEach {
                    if (it.size > 1) {
                        Toast.makeText(
                            app,
                            "Multiple work requests scheduled: ${it.size}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

    internal val mutableCommands = MutableSharedFlow<TimelapseCommand>(
        replay = 0,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    val commands = mutableCommands.asSharedFlow()

    internal val timelapseMutableState = MutableStateFlow<TimelapseInternalState>(Idle)

    internal val state = timelapseMutableState.asStateFlow()

    fun initApp(app: Application) {
        appFlow.tryEmit(app)
    }

    fun getTimelapseState(scope: CoroutineScope): Flow<TimelapseRecordingState> {
        return state.combine(scheduledInstantsFlow) { state, nextTimes ->
                when (state) {
                    Idle -> TimelapseRecordingState.Idle
                    is TimelapseInternalState.Capturing -> TimelapseRecordingState.Capturing(
                        startTime = state.startTime,
                        framesCaptured = state.framesCaptured,
                        nextFrameTime = nextTimes.firstOrNull() ?: Instant.MAX
                    )
                }
            }
            .stateIn(scope, SharingStarted.Eagerly, TimelapseRecordingState.Idle)
    }
}

sealed class TimelapseCommand {

    data object CaptureImage : TimelapseCommand()
}

internal sealed class TimelapseInternalState {

    object Idle : TimelapseInternalState()

    data class Capturing(
        val startTime: Instant,
        val framesCaptured: Int
    ) : TimelapseInternalState()
}