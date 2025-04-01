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

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import java.io.File

class OnlapseApi {
    private val client = HttpClient(OkHttp) {
        install(Logging) {
            logger = Logger.ANDROID
        }
    }

    suspend fun uploadImage(file: File, timelapseName: String): String {
        // Read file into ByteArray
        val fileBytes = file.readBytes()

        val response: HttpResponse = client.submitFormWithBinaryData(
            url = urlWithPath("upload"),
            formData = formData {
                // Ключ "file" или любой другой, который ожидает сервер
                append(
                    key = "file",
                    value = fileBytes,
                    headers = Headers.build {
                        // Specify ContentType
                        append(HttpHeaders.ContentType, ContentType.Image.JPEG.toString())
                        // We can also add some additional data here
                        append(HttpHeaders.ContentDisposition, """filename="${file.name}"""")
                        append(TIMELAPSE_NAME_HEADER, timelapseName)
                    }
                )
            }
        )

        // Предположим, что сервер возвращает строку-ответ, можно адаптировать под себя
        return response.body()
    }

    private fun urlWithPath(path: String) = "$SERVER_URL$path"

    companion object {
        const val SERVER_URL = "http://rusartur4ik.asuscomm.com:7499/"

        const val TIMELAPSE_NAME_HEADER = "Timelapse-Name"
    }
}