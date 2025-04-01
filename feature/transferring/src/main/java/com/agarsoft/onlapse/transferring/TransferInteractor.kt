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

package com.agarsoft.onlapse.transferring

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.agarsoft.onlapse.timelapse.TimelapseCommand
import com.agarsoft.onlapse.timelapse.TimelapseCommands
import com.agarsoft.onlapse.timelapse.TimelapseNameProvider.DEFAULT_TIMELAPSE_NAME
import com.google.jetpackcamera.core.common.retryWithBackoff
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class TransferInteractor @Inject constructor(
    private val application: Application,
) {

    val api = OnlapseApi()

    val timelapseCommands: Flow<TimelapseCommand> = TimelapseCommands.commands

    fun initialize(scope: CoroutineScope) {
        scope.launch {
            timelapseCommands.collect {
                when (it) {
                    is TimelapseCommand.OnImageCaptured -> {
                        sendContainingImages()
                    }
                    else -> {}
                }
            }
        }
    }

    suspend fun sendContainingImages() {
        val timelapseName = DEFAULT_TIMELAPSE_NAME
        val directory = TransferringSettings.getTempTimelapseDirectory(
            context = application,
            timelapseName = timelapseName
        )
        val imageUrls = directory.listFiles()?.toList() ?: emptyList()
        imageUrls.forEach { imageUrl ->
            try {
                retryWithBackoff {
                    api.uploadImage(imageUrl, timelapseName)
                }
                imageUrl.delete()
                Toast.makeText(application, "Image uploaded", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e("TransferInteractor", "Failed to upload image beyond all retries", e)
            }
        }
    }
}