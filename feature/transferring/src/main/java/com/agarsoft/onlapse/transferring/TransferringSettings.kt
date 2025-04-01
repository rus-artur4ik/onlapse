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

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import java.io.File
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object TransferringSettings {

    fun getTempTimelapseDirectory(
        context: Context,
        timelapseName: String,
    ): File {
        // Internal directory /data/data/<appPackage>/files/<timelapseName>
        val directory = context.filesDir.resolve(timelapseName)
        directory.mkdirs()
        return directory
    }

    fun getTimelapseShotUri(
        context: Context,
        timelapseName: String,
    ): Uri {
        val nowUtc = ZonedDateTime.now(ZoneOffset.UTC)
        // ISO_INSTANT outputs in UTC, e.g. "2021-06-15T10:00:00Z"
        val isoInstantString = nowUtc.format(DateTimeFormatter.ISO_INSTANT)

        // Internal directory /data/data/<appPackage>/files/<timelapseName>/<shotName>
        val directory = getTempTimelapseDirectory(context, timelapseName)
        val shotName = "${timelapseName}_${isoInstantString}.jpg"
        return directory.resolve(shotName).toUri()
    }
}