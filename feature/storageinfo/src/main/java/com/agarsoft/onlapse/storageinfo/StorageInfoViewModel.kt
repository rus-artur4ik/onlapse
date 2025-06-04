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

package com.agarsoft.onlapse.storageinfo

import android.app.Application
import android.content.Context.STORAGE_SERVICE
import android.os.storage.StorageManager
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.nio.file.Files
import javax.inject.Inject

class StorageInfoViewModel @Inject constructor(
    application: Application
): AndroidViewModel(application) {

    private val _state = MutableStateFlow<StorageInfoState?>(null)
    val state = _state.asStateFlow()

    init {
        val filesDir = application.filesDir
        val usedSpace = Files.size(filesDir.toPath())

        val storageManager = application.getSystemService(STORAGE_SERVICE) as StorageManager
        val storageUuid = storageManager.getUuidForPath(filesDir)

        val imagesToSend = mutableMapOf<String, StorageFile>()
        val stack = ArrayDeque<File>()
        stack.addLast(filesDir)

        while (stack.isNotEmpty()) {
            val currentFile = stack.removeLast()
            if (currentFile.isDirectory) {
                currentFile.listFiles()?.forEach { stack.addLast(it) }
            } else {
                if (currentFile.extension in imageExtensions) {
                    imagesToSend[currentFile.name] = StorageFile(
                        name = currentFile.name,
                        uri = currentFile.toUri(),
                        size = currentFile.length(),
                        createdAt = Files.getLastModifiedTime(currentFile.toPath()).toInstant(),
                        uploadedSize = 0L, // Assuming no files are uploaded yet
                    )
                }
            }
        }

        // Initialize the state with default values or fetch from a repository
        _state.value = StorageInfoState(
            usedSpace = usedSpace,
            freeSpace = storageManager.getAllocatableBytes(storageUuid),
            totalSpace = filesDir.totalSpace,
            filesToSend = imagesToSend.toMap()
        )
    }

    companion object {
        private val imageExtensions = listOf("jpg", "jpeg", "png", "gif", "webp")
    }
}