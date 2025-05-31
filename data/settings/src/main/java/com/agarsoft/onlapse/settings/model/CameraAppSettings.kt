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
package com.agarsoft.onlapse.settings.model

const val TARGET_FPS_AUTO = 0
const val UNLIMITED_VIDEO_DURATION = 0L
val DEFAULT_HDR_DYNAMIC_RANGE = DynamicRange.HLG10
val DEFAULT_HDR_IMAGE_OUTPUT = ImageOutputFormat.JPEG_ULTRA_HDR

/**
 * Data layer representation for settings.
 */
data class CameraAppSettings(
    val captureMode: CaptureMode = CaptureMode.IMAGE_ONLY,
    val cameraLensFacing: LensFacing = LensFacing.BACK,
    val darkMode: DarkMode = DarkMode.DARK,
    val flashMode: FlashMode = FlashMode.OFF,
    val streamConfig: StreamConfig = StreamConfig.SINGLE_STREAM,
    val frequencyConfig: TimelapseFrequencyConfig = TimelapseFrequencyConfig.TWENTY_FOUR_PER_DAY,
    val aspectRatio: AspectRatio = AspectRatio.NINE_SIXTEEN,
    val stabilizationMode: StabilizationMode = StabilizationMode.AUTO,
    val dynamicRange: DynamicRange = DynamicRange.SDR,
    val videoQuality: VideoQuality = VideoQuality.UNSPECIFIED,
    val zoomScale: Float = 1f,
    val targetFrameRate: Int = TARGET_FPS_AUTO,
    val imageFormat: ImageOutputFormat = ImageOutputFormat.JPEG,
    val audioEnabled: Boolean = true,
    val deviceRotation: DeviceRotation = DeviceRotation.Natural,
    val concurrentCameraMode: ConcurrentCameraMode = ConcurrentCameraMode.OFF,
    val maxVideoDurationMillis: Long = UNLIMITED_VIDEO_DURATION
)

fun SystemConstraints.forCurrentLens(cameraAppSettings: CameraAppSettings): CameraConstraints? =
    perLensConstraints[cameraAppSettings.cameraLensFacing]

val DEFAULT_CAMERA_APP_SETTINGS = CameraAppSettings()
