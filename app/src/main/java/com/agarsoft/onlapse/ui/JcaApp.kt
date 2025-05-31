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

import android.Manifest
import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.agarsoft.onlapse.BuildConfig
import com.agarsoft.onlapse.feature.postcapture.PostCaptureScreen
import com.agarsoft.onlapse.feature.preview.PreviewMode
import com.agarsoft.onlapse.feature.preview.PreviewScreen
import com.agarsoft.onlapse.permissions.PermissionsScreen
import com.agarsoft.onlapse.settings.SettingsScreen
import com.agarsoft.onlapse.settings.VersionInfoHolder
import com.agarsoft.onlapse.storageinfo.StorageInfoScreen
import com.agarsoft.onlapse.viewer.ViewerScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@Composable
fun JcaApp(
    openAppSettings: () -> Unit,
    /*TODO(b/306236646): remove after still capture*/
    previewMode: PreviewMode,
    modifier: Modifier = Modifier,
    isDebugMode: Boolean,
    onRequestWindowColorMode: (Int) -> Unit,
    onFirstFrameCaptureCompleted: () -> Unit
) {
    JetpackCameraNavHost(
        previewMode = previewMode,
        isDebugMode = isDebugMode,
        onOpenAppSettings = openAppSettings,
        onRequestWindowColorMode = onRequestWindowColorMode,
        onFirstFrameCaptureCompleted = onFirstFrameCaptureCompleted,
        modifier = modifier
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun JetpackCameraNavHost(
    modifier: Modifier = Modifier,
    previewMode: PreviewMode,
    isDebugMode: Boolean,
    onOpenAppSettings: () -> Unit,
    onRequestWindowColorMode: (Int) -> Unit,
    onFirstFrameCaptureCompleted: () -> Unit,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.MODE_SELECTION_ROUTE,
        modifier = modifier
    ) {
        composable(Routes.MODE_SELECTION_ROUTE) {
            ModeSelectionScreen(navController)
        }

        composable(Routes.VIEWER_ROUTE) {
            ViewerScreen()
        }

        composable(Routes.STORAGE_INFO_ROUTE) {
            StorageInfoScreen()
        }

        composable(Routes.PERMISSIONS_ROUTE) {
            PermissionsScreen(
                shouldRequestAudioPermission = previewMode is PreviewMode.StandardMode,
                onAllPermissionsGranted = {
                    // Pop off the permissions screen
                    navController.navigate(Routes.PREVIEW_ROUTE) {
                        popUpTo(Routes.PERMISSIONS_ROUTE) {
                            inclusive = true
                        }
                    }
                },
                openAppSettings = onOpenAppSettings
            )
        }

        composable(route = Routes.PREVIEW_ROUTE, enterTransition = { fadeIn() }) {
            val permissionStates = rememberMultiplePermissionsState(
                permissions = listOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
            // Automatically navigate to permissions screen when camera permission revoked
            LaunchedEffect(key1 = permissionStates.permissions[0].status) {
                if (!permissionStates.permissions[0].status.isGranted) {
                    // Pop off the preview screen
                    navController.navigate(Routes.PERMISSIONS_ROUTE) {
                        popUpTo(Routes.PREVIEW_ROUTE) {
                            inclusive = true
                        }
                    }
                }
            }
            PreviewScreen(
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS_ROUTE) },
                onNavigateToPostCapture = { imageUri ->
                    navController.navigate(
                        "${Routes.POST_CAPTURE_ROUTE}?imageUri=${Uri.encode(imageUri.toString())}"
                    )
                },
                onRequestWindowColorMode = onRequestWindowColorMode,
                onFirstFrameCaptureCompleted = onFirstFrameCaptureCompleted,
                previewMode = previewMode,
                isDebugMode = isDebugMode
            )
        }
        composable(
            route = Routes.SETTINGS_ROUTE,
            enterTransition = {
                fadeIn(
                    animationSpec = tween(easing = LinearEasing)
                ) + slideIntoContainer(
                    animationSpec = tween(easing = EaseIn),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    animationSpec = tween(easing = EaseOut),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }
        ) {
            SettingsScreen(
                versionInfo = VersionInfoHolder(
                    versionName = BuildConfig.VERSION_NAME,
                    buildType = BuildConfig.BUILD_TYPE
                ),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            "${Routes.POST_CAPTURE_ROUTE}?imageUri={imageUri}",
            arguments = listOf(
                navArgument("imageUri") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val imageUriString = backStackEntry.arguments?.getString("imageUri")

            val imageUri = if (!imageUriString.isNullOrEmpty()) {
                imageUriString.toUri()
            } else {
                null
            }
            PostCaptureScreen(
                imageUri = imageUri
            )
        }
    }
}
