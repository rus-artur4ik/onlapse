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

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun StorageInfoScreen(
    onOpenImage: (Uri?) -> Unit
) {
    val viewModel = viewModel<StorageInfoViewModel>()
    val state by viewModel.state.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        if (state == null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, CenterVertically),
                modifier = Modifier.fillMaxSize(),
            ) {
                CircularProgressIndicator()
                Text(text = "Loading storage info...")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Storage Info",
                        )

                        Text(
                            text = "${(state?.usedSpace ?: 0) / (1024*1024)} MiB of ${(state?.totalSpace ?: 0) / (1024*1024*1024)} GiB used",
                        )

                        Text(
                            text = "Available space: ${(state?.freeSpace ?: 0) / (1024*1024)} MiB",
                        )
                    }
                }

                items(
                    items = state?.filesToSend?.toList() ?: emptyList(),
                    key = { it.first }
                ) { item ->
                    StorageItemCard(
                        item = item.second,
                        onOpenImage = { onOpenImage(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StorageItemCard(item: StorageFile, onOpenImage: (Uri?) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val context = LocalContext.current

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(item.uri)
                    .crossfade(true)
                    .build(),
                placeholder = rememberVectorPainter(Icons.Rounded.Image),
                error = rememberVectorPainter(Icons.Rounded.BrokenImage),
                contentDescription = "Image preview",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(width = 60.dp, height = 90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenImage(item.uri) }
                    .background(color = MaterialTheme.colorScheme.surface)
            )

            Column {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                )
                val formattedDate = remember(item.createdAt) {
                    item.createdAt
                        .atZone(ZoneId.systemDefault())
                        .format(
                            DateTimeFormatter.ofLocalizedDateTime(
                                FormatStyle.MEDIUM
                            )
                        )
                }
                Text(
                    text = "Captured: $formattedDate",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Size: %.2f MiB"
                        .format(item.size.toFloat() / (1024 * 1024)),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Uploaded: %.2f MiB"
                        .format(item.uploadedSize.toFloat() / (1024 * 1024)),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Preview
@Composable
fun StorageItemCardPreview() {
    val item = StorageFile(
        name = "file.jpg",
        uri = Uri.EMPTY,
        size = 1024L * 1024L,
        createdAt = Instant.now(),
        uploadedSize = 0L
    )
    StorageItemCard(item, {})
}
