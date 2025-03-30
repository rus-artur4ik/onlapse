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

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class TimelapseWorker(
    appContext: Context,
    workerParams: WorkerParameters
): CoroutineWorker(appContext, workerParams) {

    private val timelapseFlow = TimelapseCommands.mutableFlow

    override suspend fun doWork(): Result {

        // Do the work here--in this case, upload the images.
        println("TimelapseWorker.doWork()")
        timelapseFlow.emit(TimelapseCommand.CaptureImage)
        println("capture finished")

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}