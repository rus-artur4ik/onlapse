/*
 * Copyright (C) 2023 The Android Open Source Project
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
package com.agarsoft.onlapse.feature.preview.quicksettings.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.agarsoft.onlapse.feature.preview.R
import com.agarsoft.onlapse.settings.model.AspectRatio
import com.agarsoft.onlapse.settings.model.ConcurrentCameraMode
import com.agarsoft.onlapse.settings.model.DEFAULT_HDR_DYNAMIC_RANGE
import com.agarsoft.onlapse.settings.model.DEFAULT_HDR_IMAGE_OUTPUT
import com.agarsoft.onlapse.settings.model.DynamicRange
import com.agarsoft.onlapse.settings.model.FlashMode
import com.agarsoft.onlapse.settings.model.ImageOutputFormat
import com.agarsoft.onlapse.settings.model.LensFacing
import com.agarsoft.onlapse.settings.model.StreamConfig
import com.agarsoft.onlapse.settings.model.TimelapseFrequencyConfig
import com.agarsoft.onlapse.settings.model.TimelapseFrequencyConfig.FOUR_PER_DAY
import com.agarsoft.onlapse.settings.model.TimelapseFrequencyConfig.ONE_HUNDRED_TWENTY_PER_DAY
import com.agarsoft.onlapse.settings.model.TimelapseFrequencyConfig.ONE_PER_DAY
import com.agarsoft.onlapse.settings.model.TimelapseFrequencyConfig.SIXTY_PER_DAY
import com.agarsoft.onlapse.settings.model.TimelapseFrequencyConfig.SIX_PER_DAY
import com.agarsoft.onlapse.settings.model.TimelapseFrequencyConfig.THIRTY_PER_DAY
import com.agarsoft.onlapse.settings.model.TimelapseFrequencyConfig.TWELVE_PER_DAY
import com.agarsoft.onlapse.settings.model.TimelapseFrequencyConfig.TWENTY_FOUR_PER_DAY
import com.agarsoft.onlapse.settings.model.TimelapseFrequencyConfig.TWO_PER_DAY
import kotlin.math.min

// completed components ready to go into preview screen

@Composable
fun FocusedQuickSetRatio(
    setRatio: (aspectRatio: AspectRatio) -> Unit,
    currentRatio: AspectRatio,
    modifier: Modifier = Modifier
) {
    val buttons: Array<@Composable () -> Unit> =
        arrayOf(
            {
                QuickSetRatio(
                    modifier = Modifier.testTag(QUICK_SETTINGS_RATIO_3_4_BUTTON),
                    onClick = { setRatio(AspectRatio.THREE_FOUR) },
                    ratio = AspectRatio.THREE_FOUR,
                    currentRatio = currentRatio,
                    isHighlightEnabled = true
                )
            },
            {
                QuickSetRatio(
                    modifier = Modifier.testTag(QUICK_SETTINGS_RATIO_9_16_BUTTON),
                    onClick = { setRatio(AspectRatio.NINE_SIXTEEN) },
                    ratio = AspectRatio.NINE_SIXTEEN,
                    currentRatio = currentRatio,
                    isHighlightEnabled = true
                )
            },
            {
                QuickSetRatio(
                    modifier = Modifier.testTag(QUICK_SETTINGS_RATIO_1_1_BUTTON),
                    onClick = { setRatio(AspectRatio.ONE_ONE) },
                    ratio = AspectRatio.ONE_ONE,
                    currentRatio = currentRatio,
                    isHighlightEnabled = true
                )
            }
        )
    ExpandedQuickSetting(modifier = modifier, quickSettingButtons = buttons)
}

@Composable
fun QuickSetHdr(
    modifier: Modifier = Modifier,
    onClick: (dynamicRange: DynamicRange, imageOutputFormat: ImageOutputFormat) -> Unit,
    selectedDynamicRange: DynamicRange,
    selectedImageOutputFormat: ImageOutputFormat,
    hdrDynamicRangeSupported: Boolean,
    previewMode: com.agarsoft.onlapse.feature.preview.PreviewMode,
    enabled: Boolean
) {
    val enum =
        if (selectedDynamicRange == DEFAULT_HDR_DYNAMIC_RANGE ||
            selectedImageOutputFormat == DEFAULT_HDR_IMAGE_OUTPUT
        ) {
            com.agarsoft.onlapse.feature.preview.quicksettings.CameraDynamicRange.HDR
        } else {
            com.agarsoft.onlapse.feature.preview.quicksettings.CameraDynamicRange.SDR
        }

    QuickSettingUiItem(
        modifier = modifier,
        enum = enum,
        onClick = {
            val newDynamicRange =
                if (selectedDynamicRange == DynamicRange.SDR && hdrDynamicRangeSupported) {
                    DEFAULT_HDR_DYNAMIC_RANGE
                } else {
                    DynamicRange.SDR
                }
            val newImageOutputFormat =
                if (!hdrDynamicRangeSupported ||
                    previewMode is com.agarsoft.onlapse.feature.preview.PreviewMode.ExternalImageCaptureMode
                ) {
                    DEFAULT_HDR_IMAGE_OUTPUT
                } else {
                    ImageOutputFormat.JPEG
                }
            onClick(newDynamicRange, newImageOutputFormat)
        },
        isHighLighted = (selectedDynamicRange != DynamicRange.SDR),
        enabled = enabled
    )
}

@Composable
fun QuickSetRatio(
    onClick: () -> Unit,
    ratio: AspectRatio,
    currentRatio: AspectRatio,
    modifier: Modifier = Modifier,
    isHighlightEnabled: Boolean = false
) {
    val enum =
        when (ratio) {
            AspectRatio.THREE_FOUR -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraAspectRatio.THREE_FOUR
            AspectRatio.NINE_SIXTEEN -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraAspectRatio.NINE_SIXTEEN
            AspectRatio.ONE_ONE -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraAspectRatio.ONE_ONE
            else -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraAspectRatio.ONE_ONE
        }
    QuickSettingUiItem(
        modifier = modifier,
        enum = enum,
        onClick = { onClick() },
        isHighLighted = isHighlightEnabled && (ratio == currentRatio)
    )
}

@Composable
fun QuickSetFlash(
    modifier: Modifier = Modifier,
    onClick: (FlashMode) -> Unit,
    flashModeUiState: com.agarsoft.onlapse.feature.preview.FlashModeUiState
) {
    when (flashModeUiState) {
        is com.agarsoft.onlapse.feature.preview.FlashModeUiState.Unavailable ->
            QuickSettingUiItem(
                modifier = modifier,
                enum = com.agarsoft.onlapse.feature.preview.quicksettings.CameraFlashMode.OFF,
                enabled = false,
                onClick = {}
            )
        is com.agarsoft.onlapse.feature.preview.FlashModeUiState.Available ->
            QuickSettingUiItem(
                modifier = modifier,
                enum = flashModeUiState.selectedFlashMode.toCameraFlashMode(
                    flashModeUiState.isActive
                ),
                isHighLighted = flashModeUiState.selectedFlashMode == FlashMode.ON,
                onClick = {
                    onClick(flashModeUiState.getNextFlashMode())
                }
            )
    }
}

@Composable
fun QuickFlipCamera(
    setLensFacing: (LensFacing) -> Unit,
    currentLensFacing: LensFacing,
    modifier: Modifier = Modifier
) {
    val enum =
        when (currentLensFacing) {
            LensFacing.FRONT -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraLensFace.FRONT
            LensFacing.BACK -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraLensFace.BACK
        }
    QuickSettingUiItem(
        modifier = modifier,
        enum = enum,
        onClick = { setLensFacing(currentLensFacing.flip()) }
    )
}

@Composable
fun QuickSetStreamConfig(
    setStreamConfig: (StreamConfig) -> Unit,
    currentStreamConfig: StreamConfig,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val enum: com.agarsoft.onlapse.feature.preview.quicksettings.CameraStreamConfig =
        when (currentStreamConfig) {
            StreamConfig.MULTI_STREAM -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraStreamConfig.MULTI_STREAM
            StreamConfig.SINGLE_STREAM -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraStreamConfig.SINGLE_STREAM
        }
    QuickSettingUiItem(
        modifier = modifier,
        enum = enum,
        onClick = {
            when (currentStreamConfig) {
                StreamConfig.MULTI_STREAM -> setStreamConfig(StreamConfig.SINGLE_STREAM)
                StreamConfig.SINGLE_STREAM -> setStreamConfig(StreamConfig.MULTI_STREAM)
            }
        },
        enabled = enabled
    )
}

@Composable
fun QuickSetConcurrentCamera(
    setConcurrentCameraMode: (ConcurrentCameraMode) -> Unit,
    currentConcurrentCameraMode: ConcurrentCameraMode,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val enum: com.agarsoft.onlapse.feature.preview.quicksettings.CameraConcurrentCameraMode =
        when (currentConcurrentCameraMode) {
            ConcurrentCameraMode.OFF -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraConcurrentCameraMode.OFF
            ConcurrentCameraMode.DUAL -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraConcurrentCameraMode.DUAL
        }
    QuickSettingUiItem(
        modifier = modifier,
        enum = enum,
        onClick = {
            when (currentConcurrentCameraMode) {
                ConcurrentCameraMode.OFF -> setConcurrentCameraMode(ConcurrentCameraMode.DUAL)
                ConcurrentCameraMode.DUAL -> setConcurrentCameraMode(ConcurrentCameraMode.OFF)
            }
        },
        enabled = enabled
    )
}

@Composable
fun QuickSetTimelapseFrequencyConfig(
    setTimelapseConfig: (TimelapseFrequencyConfig) -> Unit,
    currentFrequencyConfig: TimelapseFrequencyConfig,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val enum: com.agarsoft.onlapse.feature.preview.quicksettings.CameraTimelapseFrequency = when (currentFrequencyConfig) {
            ONE_PER_DAY -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraTimelapseFrequency.ONE_PER_DAY
            TWO_PER_DAY -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraTimelapseFrequency.TWO_PER_DAY
            FOUR_PER_DAY -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraTimelapseFrequency.FOUR_PER_DAY
            SIX_PER_DAY -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraTimelapseFrequency.SIX_PER_DAY
            TWELVE_PER_DAY -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraTimelapseFrequency.TWELVE_PER_DAY
            TWENTY_FOUR_PER_DAY -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraTimelapseFrequency.TWENTY_FOUR_PER_DAY
            THIRTY_PER_DAY -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraTimelapseFrequency.THIRTY_PER_DAY
            SIXTY_PER_DAY -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraTimelapseFrequency.SIXTY_PER_DAY
            ONE_HUNDRED_TWENTY_PER_DAY -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraTimelapseFrequency.ONE_HUNDRED_TWENTY_PER_DAY
        }
    QuickSettingUiItem(
        modifier = modifier,
        enum = enum,
        onClick = {
            when (currentFrequencyConfig) {
                ONE_PER_DAY -> setTimelapseConfig(TWO_PER_DAY)
                TWO_PER_DAY -> setTimelapseConfig(FOUR_PER_DAY)
                FOUR_PER_DAY -> setTimelapseConfig(SIX_PER_DAY)
                SIX_PER_DAY -> setTimelapseConfig(TWELVE_PER_DAY)
                TWELVE_PER_DAY -> setTimelapseConfig(TWENTY_FOUR_PER_DAY)
                TWENTY_FOUR_PER_DAY -> setTimelapseConfig(THIRTY_PER_DAY)
                THIRTY_PER_DAY -> setTimelapseConfig(SIXTY_PER_DAY)
                SIXTY_PER_DAY -> setTimelapseConfig(ONE_HUNDRED_TWENTY_PER_DAY)
                ONE_HUNDRED_TWENTY_PER_DAY -> setTimelapseConfig(ONE_PER_DAY)
            }
        },
        enabled = enabled
    )
}

/**
 * Button to toggle quick settings
 */
@Composable
fun ToggleQuickSettingsButton(
    toggleDropDown: () -> Unit,
    isOpen: Boolean,
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isOpen) -180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow) // Adjust duration as needed
    )
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.rotate(rotationAngle)
    ) {
        // dropdown icon
        Icon(
            imageVector = Icons.Filled.ExpandMore,
            contentDescription = if (isOpen) {
                stringResource(R.string.quick_settings_dropdown_open_description)
            } else {
                stringResource(R.string.quick_settings_dropdown_closed_description)
            },
            modifier = Modifier
                .testTag(QUICK_SETTINGS_DROP_DOWN)
                .size(72.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    // removes the greyish background animation that appears when clicking on a clickable
                    indication = null,
                    onClick = toggleDropDown
                )
        )
    }
}

// subcomponents used to build completed components

@Composable
fun QuickSettingUiItem(
    enum: com.agarsoft.onlapse.feature.preview.quicksettings.QuickSettingsEnum,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHighLighted: Boolean = false,
    enabled: Boolean = true
) {
    QuickSettingUiItem(
        modifier = modifier,
        painter = enum.getPainter(),
        text = stringResource(id = enum.getTextResId()),
        accessibilityText = stringResource(id = enum.getDescriptionResId()),
        onClick = { onClick() },
        isHighLighted = isHighLighted,
        enabled = enabled
    )
}

/**
 * The itemized UI component representing each button in quick settings.
 */
@Composable
fun QuickSettingUiItem(
    text: String,
    painter: Painter,
    accessibilityText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHighLighted: Boolean = false,
    enabled: Boolean = true
) {
    val iconSize = dimensionResource(id = R.dimen.quick_settings_ui_item_icon_size)

    var buttonClicked by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (buttonClicked) 1.1f else 1f, // Scale up to 110%
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        finishedListener = {
            buttonClicked = false // Reset the trigger
        }
    )
    Column(
        modifier =
        modifier
            .wrapContentSize()
            .padding(dimensionResource(id = R.dimen.quick_settings_ui_item_padding))
            .clickable(
                enabled = enabled,
                onClick = {
                    buttonClicked = true
                    onClick()
                },
                indication = null,
                interactionSource = null
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val contentColor = (if (isHighLighted) Color.Yellow else Color.White).let {
            // When in disabled state, material3 guidelines say the element's opacity should be 38%
            // See: https://m3.material.io/foundations/interaction/states/applying-states#3c3032e8-b07a-42ac-a508-a32f573cc7e1
            // and: https://developer.android.com/develop/ui/compose/designsystems/material2-material3#emphasis-and
            if (!enabled) it.copy(alpha = 0.38f) else it
        }
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Icon(
                painter = painter,
                contentDescription = accessibilityText,
                modifier = Modifier.size(iconSize).scale(animatedScale)
            )

            Text(text = text, textAlign = TextAlign.Center)
        }
    }
}

/**
 * Should you want to have an expanded view of a single quick setting
 */
@Composable
fun ExpandedQuickSetting(
    modifier: Modifier = Modifier,
    vararg quickSettingButtons: @Composable () -> Unit
) {
    val expandedNumOfColumns =
        min(
            quickSettingButtons.size,
            (
                (
                    LocalConfiguration.current.screenWidthDp.dp - (
                        dimensionResource(
                            id = R.dimen.quick_settings_ui_horizontal_padding
                        ) * 2
                        )
                    ) /
                    (
                        dimensionResource(id = R.dimen.quick_settings_ui_item_icon_size) +
                            (dimensionResource(id = R.dimen.quick_settings_ui_item_padding) * 2)
                        )
                ).toInt()
        )
    LazyVerticalGrid(
        modifier = modifier.fillMaxWidth(),
        columns = GridCells.Fixed(count = expandedNumOfColumns)
    ) {
        items(quickSettingButtons.size) { i ->
            quickSettingButtons[i]()
        }
    }
}

/**
 * Algorithm to determine dimensions of QuickSettings Icon layout
 */
@Composable
fun QuickSettingsGrid(
    modifier: Modifier = Modifier,
    quickSettingsButtons: List<@Composable () -> Unit>
) {
    val initialNumOfColumns =
        min(
            quickSettingsButtons.size,
            (
                (
                    LocalConfiguration.current.screenWidthDp.dp - (
                        dimensionResource(
                            id = R.dimen.quick_settings_ui_horizontal_padding
                        ) * 2
                        )
                    ) /
                    (
                        dimensionResource(id = R.dimen.quick_settings_ui_item_icon_size) +
                            (dimensionResource(id = R.dimen.quick_settings_ui_item_padding) * 2)
                        )
                ).toInt()
        )

    LazyVerticalGrid(
        modifier = modifier.fillMaxWidth(),
        columns = GridCells.Fixed(count = initialNumOfColumns)
    ) {
        items(quickSettingsButtons.size) { i ->
            quickSettingsButtons[i]()
        }
    }
}

/**
 * The top bar indicators for quick settings items.
 */
@Composable
fun TopBarSettingIndicator(
    enum: com.agarsoft.onlapse.feature.preview.quicksettings.QuickSettingsEnum,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val contentColor = Color.White.let {
        if (!enabled) it.copy(alpha = 0.38f) else it
    }
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Icon(
            painter = enum.getPainter(),
            contentDescription = stringResource(id = enum.getDescriptionResId()),
            modifier = modifier
                .size(dimensionResource(id = R.dimen.quick_settings_indicator_size))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                    enabled = enabled
                )
        )
    }
}

@Composable
fun FlashModeIndicator(
    flashModeUiState: com.agarsoft.onlapse.feature.preview.FlashModeUiState,
    onClick: (flashMode: FlashMode) -> Unit
) {
    when (flashModeUiState) {
        is com.agarsoft.onlapse.feature.preview.FlashModeUiState.Unavailable ->
            TopBarSettingIndicator(
                enum = com.agarsoft.onlapse.feature.preview.quicksettings.CameraFlashMode.OFF,
                enabled = false
            )
        is com.agarsoft.onlapse.feature.preview.FlashModeUiState.Available ->
            TopBarSettingIndicator(
                enum = flashModeUiState.selectedFlashMode.toCameraFlashMode(
                    flashModeUiState.isActive
                ),
                onClick = {
                    onClick(flashModeUiState.getNextFlashMode())
                }
            )
    }
}

@Composable
fun QuickSettingsIndicators(
    modifier: Modifier = Modifier,
    flashModeUiState: com.agarsoft.onlapse.feature.preview.FlashModeUiState,
    onFlashModeClick: (flashMode: FlashMode) -> Unit
) {
    Row(modifier = modifier) {
        FlashModeIndicator(
            flashModeUiState,
            onFlashModeClick
        )
    }
}

private fun com.agarsoft.onlapse.feature.preview.FlashModeUiState.Available.getNextFlashMode(): FlashMode = availableFlashModes.run {
    get((indexOf(selectedFlashMode) + 1) % size)
}

private fun FlashMode.toCameraFlashMode(isActive: Boolean) = when (this) {
    FlashMode.OFF -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraFlashMode.OFF
    FlashMode.AUTO -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraFlashMode.AUTO
    FlashMode.ON -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraFlashMode.ON
    FlashMode.LOW_LIGHT_BOOST -> {
        when (isActive) {
            true -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraFlashMode.LOW_LIGHT_BOOST_ACTIVE
            false -> com.agarsoft.onlapse.feature.preview.quicksettings.CameraFlashMode.LOW_LIGHT_BOOST_INACTIVE
        }
    }
}
