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

package com.agarsoft.onlapse.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ModeSelectionScreen(navController: NavController?) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(paddingValues)
        ) {
            items(Mode.entries) { mode ->
                ModeCard(mode, navController)
            }
        }
    }
}

@Composable
private fun ModeCard(mode: Mode, navController: NavController?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                when (mode) {
                    Mode.CAMERA -> {
                        navController?.navigate(Routes.PERMISSIONS_ROUTE)
                    }
                    Mode.VIEWER -> {
                        navController?.navigate(Routes.VIEWER_ROUTE)
                    }
                    Mode.STORAGE -> {
                        navController?.navigate(Routes.STORAGE_INFO_ROUTE)
                    }
                }
            }
    ) {
        Text(
            text = mode.label,
            modifier = Modifier.padding(8.dp)
        )
    }

}