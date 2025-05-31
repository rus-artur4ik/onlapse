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
package com.agarsoft.onlapse.feature.preview

import android.util.Size
import com.agarsoft.onlapse.core.camera.TimelapseRecordingState
import com.agarsoft.onlapse.core.camera.VideoRecordingState
import java.util.LinkedList
import java.util.Queue

/**
 * Defines the current state of the [PreviewScreen].
 */
sealed interface PreviewUiState {
    data object NotReady : PreviewUiState

    data class Ready(
        // "quick" settings
        val currentCameraSettings: com.agarsoft.onlapse.settings.model.CameraAppSettings = _root_ide_package_.com.agarsoft.onlapse.settings.model.CameraAppSettings(),
        val systemConstraints: com.agarsoft.onlapse.settings.model.SystemConstraints = _root_ide_package_.com.agarsoft.onlapse.settings.model.SystemConstraints(),
        val zoomScale: Float = 1f,
        val videoRecordingState: VideoRecordingState = VideoRecordingState.Inactive(),
        val timelapseRecordingState: TimelapseRecordingState = TimelapseRecordingState.Idle,
        val quickSettingsIsOpen: Boolean = false,

        // todo: remove after implementing post capture screen
        val toastMessageToShow: com.agarsoft.onlapse.feature.preview.ui.ToastMessage? = null,
        val snackBarQueue: Queue<com.agarsoft.onlapse.feature.preview.ui.SnackbarData> = LinkedList(),
        val lastBlinkTimeStamp: Long = 0,
        val previewMode: PreviewMode = PreviewMode.StandardMode {},
        val captureModeToggleUiState: CaptureModeToggleUiState = CaptureModeToggleUiState.Invisible,
        val sessionFirstFrameTimestamp: Long = 0L,
        val currentPhysicalCameraId: String? = null,
        val currentLogicalCameraId: String? = null,
        val debugUiState: DebugUiState = DebugUiState(),
        val stabilizationUiState: StabilizationUiState = StabilizationUiState.Disabled,
        val flashModeUiState: FlashModeUiState = FlashModeUiState.Unavailable,
        val videoQuality: com.agarsoft.onlapse.settings.model.VideoQuality = com.agarsoft.onlapse.settings.model.VideoQuality.UNSPECIFIED,
        val audioUiState: AudioUiState = AudioUiState.Disabled,
        val elapsedTimeUiState: ElapsedTimeUiState = ElapsedTimeUiState.Unavailable,
        val captureButtonUiState: CaptureButtonUiState = CaptureButtonUiState.Unavailable,
        val imageWellUiState: com.agarsoft.onlapse.feature.preview.ui.ImageWellUiState = com.agarsoft.onlapse.feature.preview.ui.ImageWellUiState.NoPreviousCapture
    ) : PreviewUiState
}

data class DebugUiState(
    val cameraPropertiesJSON: String = "",
    val videoResolution: Size? = null,
    val isDebugMode: Boolean = false,
    val isDebugOverlayOpen: Boolean = false
)
val DEFAULT_CAPTURE_BUTTON_STATE = CaptureButtonUiState.Enabled.Idle

sealed interface CaptureButtonUiState {
    data object Unavailable : CaptureButtonUiState
    sealed interface Enabled : CaptureButtonUiState {
        data object Idle : Enabled
        data object RecordingTimelapse : Enabled
    }
}
sealed interface ElapsedTimeUiState {
    data object Unavailable : ElapsedTimeUiState

    data class Enabled(val elapsedTimeNanos: Long) : ElapsedTimeUiState
}

sealed interface AudioUiState {
    val amplitude: Double

    sealed interface Enabled : AudioUiState {
        data class On(override val amplitude: Double) : Enabled
        data object Mute : Enabled {
            override val amplitude = 0.0
        }
    }

    // todo give a disabledreason when audio permission is not granted
    data object Disabled : AudioUiState {
        override val amplitude = 0.0
    }
}

sealed interface StabilizationUiState {
    data object Disabled : StabilizationUiState

    sealed interface Enabled : StabilizationUiState {
        val stabilizationMode: com.agarsoft.onlapse.settings.model.StabilizationMode
        val active: Boolean
    }

    data class Specific(
        override val stabilizationMode: com.agarsoft.onlapse.settings.model.StabilizationMode,
        override val active: Boolean = true
    ) : Enabled {
        init {
            require(stabilizationMode != com.agarsoft.onlapse.settings.model.StabilizationMode.AUTO) {
                "Specific StabilizationUiState cannot have AUTO stabilization mode."
            }
        }
    }

    data class Auto(override val stabilizationMode: com.agarsoft.onlapse.settings.model.StabilizationMode) : Enabled {
        override val active = true
    }
}

sealed class FlashModeUiState {
    data object Unavailable : FlashModeUiState()

    data class Available(
        val selectedFlashMode: com.agarsoft.onlapse.settings.model.FlashMode,
        val availableFlashModes: List<com.agarsoft.onlapse.settings.model.FlashMode>,
        val isActive: Boolean
    ) : FlashModeUiState() {
        init {
            check(selectedFlashMode in availableFlashModes) {
                "Selected flash mode of $selectedFlashMode not in available modes: " +
                    "$availableFlashModes"
            }
        }
    }

    companion object {
        private val ORDERED_UI_SUPPORTED_FLASH_MODES = listOf(
            com.agarsoft.onlapse.settings.model.FlashMode.OFF,
            com.agarsoft.onlapse.settings.model.FlashMode.ON,
            com.agarsoft.onlapse.settings.model.FlashMode.AUTO,
            com.agarsoft.onlapse.settings.model.FlashMode.LOW_LIGHT_BOOST
        )

        /**
         * Creates a FlashModeUiState from a selected flash mode and a set of supported flash modes
         * that may not include flash modes supported by the UI.
         */
        fun createFrom(
            selectedFlashMode: com.agarsoft.onlapse.settings.model.FlashMode,
            supportedFlashModes: Set<com.agarsoft.onlapse.settings.model.FlashMode>
        ): FlashModeUiState {
            // Ensure we at least support one flash mode
            check(supportedFlashModes.isNotEmpty()) {
                "No flash modes supported. Should at least support OFF."
            }

            // Convert available flash modes to list we support in the UI in our desired order
            val availableModes = ORDERED_UI_SUPPORTED_FLASH_MODES.filter {
                it in supportedFlashModes
            }

            return if (availableModes.isEmpty() || availableModes == listOf(com.agarsoft.onlapse.settings.model.FlashMode.OFF)) {
                // If we only support OFF, then return "Unavailable".
                Unavailable
            } else {
                Available(
                    selectedFlashMode = selectedFlashMode,
                    availableFlashModes = availableModes,
                    isActive = false
                )
            }
        }
    }
}
