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
package com.google.jetpackcamera.feature.preview.ui

import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import com.google.jetpackcamera.feature.preview.CaptureButtonUiState

private const val DEFAULT_CAPTURE_BUTTON_SIZE = 80f

// scales against the size of the capture button
private const val LOCK_SWITCH_PRESSED_NUCLEUS_SCALE = .5f

// scales against the size of the capture button
private const val LOCK_SWITCH_WIDTH_SCALE = 1.375f

// scales against the size of the pressed nucleus
private const val LOCK_SWITCH_HEIGHT_SCALE = 1.4f

// 1f = left, 0f = right
private const val LOCK_SWITCH_POSITION_ON = 1f
private const val LOCK_SWITCH_POSITION_OFF = 0f
private const val MINIMUM_LOCK_THRESHOLD = .65F

private const val LOCK_SWITCH_ALPHA = .37f
private enum class CaptureSource {
    CAPTURE_BUTTON,
    VOLUME_UP,
    VOLUME_DOWN
}

/**
 * Handler for using certain key events buttons as capture buttons.
 */
@Composable
private fun CaptureKeyHandler(
    onPress: (CaptureSource) -> Unit,
    onRelease: (CaptureSource) -> Unit
) {
    val view = LocalView.current
    val currentOnPress by rememberUpdatedState(onPress)
    val currentOnRelease by rememberUpdatedState(onRelease)

    fun keyCodeToCaptureSource(keyCode: Int): CaptureSource = when (keyCode) {
        KeyEvent.KEYCODE_VOLUME_UP -> CaptureSource.VOLUME_UP
        KeyEvent.KEYCODE_VOLUME_DOWN -> CaptureSource.VOLUME_DOWN
        else -> TODO("Keycode not assigned to CaptureSource")
    }

    DisposableEffect(view) {
        // todo call once per keydown
        var keyActionDown: Int? = null
        val keyEventDispatcher = ViewCompat.OnUnhandledKeyEventListenerCompat { _, event ->
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    val captureSource = keyCodeToCaptureSource(event.keyCode)
                    // pressed down
                    if (event.action == KeyEvent.ACTION_DOWN && keyActionDown == null) {
                        keyActionDown = event.keyCode
                        currentOnPress(captureSource)
                    }
                    // released
                    if (event.action == KeyEvent.ACTION_UP && keyActionDown == event.keyCode) {
                        keyActionDown = null
                        currentOnRelease(captureSource)
                    }
                    // consume the event
                    true
                }
                else -> {
                    false
                }
            }
        }

        ViewCompat.addOnUnhandledKeyEventListener(view, keyEventDispatcher)

        onDispose {
            ViewCompat.removeOnUnhandledKeyEventListener(view, keyEventDispatcher)
        }
    }
}

@Composable
fun CaptureButton(
    modifier: Modifier = Modifier,
    onImageCapture: () -> Unit,
    onLockVideoRecording: (Boolean) -> Unit,
    captureButtonUiState: CaptureButtonUiState,
    captureButtonSize: Float = DEFAULT_CAPTURE_BUTTON_SIZE
) {
    var currentUiState = rememberUpdatedState(captureButtonUiState)
    val firstKeyPressed = remember { mutableStateOf<CaptureSource?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(captureButtonUiState) {
        if (captureButtonUiState is CaptureButtonUiState.Enabled.Idle) {
            onLockVideoRecording(false)
        }
    }

    fun onPress(captureSource: CaptureSource) {
        if (firstKeyPressed.value == null) {
            firstKeyPressed.value = captureSource
        }
    }

    fun onKeyUp(captureSource: CaptureSource) {
        // releasing while pressed recording
        if (firstKeyPressed.value == captureSource) {
            when (currentUiState.value) {
                is CaptureButtonUiState.Enabled.Idle -> onImageCapture()

                CaptureButtonUiState.Enabled.RecordingTimelapse,
                CaptureButtonUiState.Unavailable -> {
                }
            }
            firstKeyPressed.value = null
        }
    }

    CaptureKeyHandler(
        onPress = { captureSource -> onPress(captureSource) },
        onRelease = { captureSource -> onKeyUp(captureSource) }
    )
    CaptureButton(
        modifier = modifier,
        onPress = { onPress(CaptureSource.CAPTURE_BUTTON) },
        onRelease = { onKeyUp(CaptureSource.CAPTURE_BUTTON) },
        onLockVideoRecording = onLockVideoRecording,
        captureButtonUiState = captureButtonUiState,
        captureButtonSize = captureButtonSize
    )
}

@Composable
private fun CaptureButton(
    modifier: Modifier = Modifier,
    onPress: () -> Unit,
    onRelease: (isLocked: Boolean) -> Unit,
    onLockVideoRecording: (Boolean) -> Unit,
    captureButtonUiState: CaptureButtonUiState,
    useLockSwitch: Boolean = true,
    captureButtonSize: Float = DEFAULT_CAPTURE_BUTTON_SIZE
) {
    // todo: explore MutableInteractionSource
    var isCaptureButtonPressed by remember {
        mutableStateOf(false)
    }

    var switchPosition by remember {
        mutableFloatStateOf(LOCK_SWITCH_POSITION_OFF)
    }
    val switchWidth = (captureButtonSize * LOCK_SWITCH_WIDTH_SCALE).dp

    val currentColor = LocalContentColor.current

    fun shouldBeLocked(): Boolean = switchPosition > MINIMUM_LOCK_THRESHOLD
    fun toggleSwitchPosition() = if (shouldBeLocked()) {
        switchPosition = LOCK_SWITCH_POSITION_OFF
    } else {
        if (isCaptureButtonPressed == false) {
            onLockVideoRecording(true)
        } else {
            switchPosition =
                LOCK_SWITCH_POSITION_ON
        }
    }
    CaptureButtonRing(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    // onLongPress cannot be null, otherwise it won't detect the release if the
                    // touch is dragged off the component
                    onLongPress = {},
                    onPress = {
                        isCaptureButtonPressed = true
                        onPress()
                        awaitRelease()
                        isCaptureButtonPressed = false
                        if (shouldBeLocked()) {
                            onLockVideoRecording(true)
                            onRelease(true)
                        }

                        switchPosition = LOCK_SWITCH_POSITION_OFF
                        onRelease(false)
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { },
                    onDragEnd = { },
                    onDrag = { change, dragAmount ->
                        if (useLockSwitch) {
                            val newPosition =
                                switchPosition - (dragAmount.x / switchWidth.toPx())
                            switchPosition =
                                newPosition.coerceIn(
                                    LOCK_SWITCH_POSITION_OFF,
                                    LOCK_SWITCH_POSITION_ON
                                )
                            change.consume()
                        }
                    }
                )
            },
        captureButtonSize = captureButtonSize,
        color = currentColor
    ) {
        CaptureButtonNucleus(
            captureButtonUiState = captureButtonUiState,
            isPressed = isCaptureButtonPressed,
            captureButtonSize = captureButtonSize
        )
    }
}

@Composable
fun CaptureButtonRing(
    modifier: Modifier = Modifier,
    captureButtonSize: Float,
    color: Color,
    contents: (@Composable () -> Unit)? = null
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        contents?.invoke()
        // todo(): use a canvas instead of a box.
        //  the sizing gets funny so the scales need to be completely readjusted
        Icon(
            imageVector = Icons.Default.Timelapse,
            tint = color,
            contentDescription = null,
            modifier = Modifier
                .size(captureButtonSize.dp)
        )
    }
}

/**
 * A nucleus for the capture button can be dragged to lock the pressed video recording.
 */
@Composable
private fun LockSwitchCaptureButtonNucleus(
    modifier: Modifier = Modifier,
    captureButtonUiState: CaptureButtonUiState,
    captureButtonSize: Float,
    switchWidth: Dp,
    switchPosition: Float,
    onToggleSwitchPosition: () -> Unit,
    shouldBeLocked: () -> Boolean
) {
    val pressedNucleusSize = (captureButtonSize * LOCK_SWITCH_PRESSED_NUCLEUS_SCALE).dp
    val switchHeight = (pressedNucleusSize * LOCK_SWITCH_HEIGHT_SCALE)

    Box(
        modifier = modifier
            .width(switchWidth),
        contentAlignment = Alignment.Center

    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .width(switchWidth)
                .height(switchHeight)
                .offset(x = -(switchWidth - pressedNucleusSize) / 2)
        ) {
            // grey cylinder offset to the left and fades in when pressed recording
            AnimatedVisibility(
                visible = captureButtonUiState ==
                    CaptureButtonUiState.Enabled.RecordingTimelapse,
                enter = fadeIn(),
                exit = ExitTransition.None
            ) {
                // grey cylinder
                Canvas(
                    modifier = Modifier
                        .size(switchWidth, switchHeight)
                        .alpha(LOCK_SWITCH_ALPHA)
                ) {
                    drawRoundRect(
                        color = Color.Black,
                        cornerRadius = CornerRadius((switchWidth / 2).toPx())
                    )
                }
            }
        }

        // small moveable Circle remains centered.
        // is behind lock icon but in front of the switch background

        CaptureButtonNucleus(
            offsetX = (-(switchWidth - pressedNucleusSize) * switchPosition),
            captureButtonSize = captureButtonSize,
            captureButtonUiState = captureButtonUiState,
            pressedVideoCaptureScale = LOCK_SWITCH_PRESSED_NUCLEUS_SCALE,
            isPressed = false
        )

        // locked icon, matches cylinder offset
        AnimatedVisibility(
            visible = captureButtonUiState ==
                CaptureButtonUiState.Enabled.RecordingTimelapse,
            enter = fadeIn(),
            exit = ExitTransition.None
        ) {
            Icon(
                modifier = Modifier
                    .size(switchHeight * .75f)
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .offset(x = -(switchWidth - pressedNucleusSize))
                    .clickable(indication = null, interactionSource = null) {
                        onToggleSwitchPosition()
                    },
                tint = Color.White,
                imageVector = if (shouldBeLocked()) {
                    Icons.Default.Lock
                } else {
                    Icons.Default.LockOpen
                },
                contentDescription = null
            )
        }
    }
}

/**
 * The animated center of the capture button. It serves as a visual indicator of the current capture and recording states.
 *
 * @param captureButtonSize diameter of the capture button ring that this is scaled to
 * @param isPressed true if the capture button is physically pressed on
 * @param offsetX the offset of this component. 0 by default
 * @param idleImageCaptureScale the scale factor for the idle size of the image-only nucleus. Must be between 0 and 1.
 * @param idleVideoCaptureScale the scale factor for the idle size of the video-only nucleus. Must be between 0 and 1.
 * @param pressedVideoCaptureScale the scale factor for the pressed size of the video-only nucleus. Must be between 0 and 1.
 */
@Composable
private fun CaptureButtonNucleus(
    modifier: Modifier = Modifier,
    captureButtonUiState: CaptureButtonUiState,
    isPressed: Boolean,
    captureButtonSize: Float,
    offsetX: Dp = 0.dp,
    recordingColor: Color = Color.Red,
    imageCaptureModeColor: Color = Color.Black,
    idleImageCaptureScale: Float = .7f,
    idleVideoCaptureScale: Float = .35f,
    pressedVideoCaptureScale: Float = .7f
) {
    require(idleImageCaptureScale in 0f..1f) {
        "value must be between 0 and 1 to remain within the bounds of the capture button"
    }
    require(idleVideoCaptureScale in 0f..1f) {
        "value must be between 0 and 1 to remain within the bounds of the capture button"
    }
    require(pressedVideoCaptureScale in 0f..1f) {
        "value must be between 0 and 1 to remain within the bounds of the capture button"
    }

    val currentUiState = rememberUpdatedState(captureButtonUiState)

    // smoothly animate between the size changes of the capture button center
    val centerShapeSize by animateDpAsState(
        targetValue = when (val uiState = currentUiState.value) {
            // inner circle fills white ring when locked
            CaptureButtonUiState.Enabled.RecordingTimelapse ->
                (captureButtonSize * pressedVideoCaptureScale).dp

            CaptureButtonUiState.Unavailable -> 0.dp
            is CaptureButtonUiState.Enabled.Idle -> (captureButtonSize * idleImageCaptureScale).dp
        },
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
    )

    // used to fade between red/white in the center of the capture button
    val animatedColor by animateColorAsState(
        targetValue = when (currentUiState.value) {
            is CaptureButtonUiState.Enabled.Idle -> imageCaptureModeColor

            is CaptureButtonUiState.Enabled.RecordingTimelapse -> recordingColor
            is CaptureButtonUiState.Unavailable -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 500)
    )

    // this box contains and centers everything
    Box(modifier = modifier.offset(x = offsetX), contentAlignment = Alignment.Center) {
        // this box is the inner circle
        Box(modifier = Modifier) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(centerShapeSize)
                    .clip(CircleShape)
                    .alpha(
                        if (isPressed &&
                            currentUiState.value == CaptureButtonUiState.Enabled.Idle
                        ) {
                            .5f // transparency to indicate click ONLY on IMAGE_ONLY
                        } else {
                            1f // solid alpha the rest of the time
                        }
                    )
                    .background(animatedColor)
            ) {}
        }
    }
}

@Preview
@Composable
private fun IdleStandardCaptureButtonPreview() {
    CaptureButtonRing(captureButtonSize = DEFAULT_CAPTURE_BUTTON_SIZE, color = Color.White) {
        CaptureButtonNucleus(
            captureButtonUiState = CaptureButtonUiState.Enabled.Idle,
            isPressed = false,
            captureButtonSize = DEFAULT_CAPTURE_BUTTON_SIZE
        )
    }
}

@Preview
@Composable
private fun IdleImageCaptureButtonPreview() {
    CaptureButtonRing(captureButtonSize = DEFAULT_CAPTURE_BUTTON_SIZE, color = Color.White) {
        CaptureButtonNucleus(
            captureButtonUiState = CaptureButtonUiState.Enabled.RecordingTimelapse,
            isPressed = false,
            captureButtonSize = DEFAULT_CAPTURE_BUTTON_SIZE
        )
    }
}

@Preview
@Composable
private fun PressedImageCaptureButtonPreview() {
    CaptureButtonRing(captureButtonSize = DEFAULT_CAPTURE_BUTTON_SIZE, color = Color.White) {
        CaptureButtonNucleus(
            captureButtonUiState = CaptureButtonUiState.Enabled.Idle,
            isPressed = true,
            captureButtonSize = DEFAULT_CAPTURE_BUTTON_SIZE
        )
    }
}

@Preview
@Composable
private fun IdleRecordingCaptureButtonPreview() {
    CaptureButtonRing(captureButtonSize = DEFAULT_CAPTURE_BUTTON_SIZE, color = Color.White) {
        CaptureButtonNucleus(
            captureButtonUiState = CaptureButtonUiState.Enabled.Idle,
            isPressed = false,
            captureButtonSize = DEFAULT_CAPTURE_BUTTON_SIZE
        )
    }
}

@Preview
@Composable
private fun SimpleNucleusPressedRecordingPreview() {
    CaptureButtonRing(captureButtonSize = DEFAULT_CAPTURE_BUTTON_SIZE, color = Color.White) {
        CaptureButtonNucleus(
            captureButtonUiState = CaptureButtonUiState.Enabled.RecordingTimelapse,
            isPressed = true,
            captureButtonSize = DEFAULT_CAPTURE_BUTTON_SIZE
        )
    }
}

@Preview
@Composable
private fun LockSwitchUnlockedPressedRecordingPreview() {
    // box is here to account for the offset lock switch
    Box(modifier = Modifier.width(150.dp), contentAlignment = Alignment.CenterEnd) {
        CaptureButtonRing(captureButtonSize = DEFAULT_CAPTURE_BUTTON_SIZE, color = Color.White) {
            LockSwitchCaptureButtonNucleus(
                captureButtonSize = DEFAULT_CAPTURE_BUTTON_SIZE,
                captureButtonUiState = CaptureButtonUiState.Enabled.RecordingTimelapse,
                switchWidth = (DEFAULT_CAPTURE_BUTTON_SIZE * LOCK_SWITCH_WIDTH_SCALE).dp,
                switchPosition = 0f,
                onToggleSwitchPosition = {},
                shouldBeLocked = { false }
            )
        }
    }
}

@Preview
@Composable
private fun LockSwitchLockedAtThresholdPressedRecordingPreview() {
    // box is here to account for the offset lock switch
    Box(modifier = Modifier.width(150.dp), contentAlignment = Alignment.CenterEnd) {
        CaptureButtonRing(captureButtonSize = DEFAULT_CAPTURE_BUTTON_SIZE, color = Color.White) {
            LockSwitchCaptureButtonNucleus(
                captureButtonSize = DEFAULT_CAPTURE_BUTTON_SIZE,
                captureButtonUiState = CaptureButtonUiState.Enabled.RecordingTimelapse,
                switchWidth = (DEFAULT_CAPTURE_BUTTON_SIZE * LOCK_SWITCH_WIDTH_SCALE).dp,
                switchPosition = MINIMUM_LOCK_THRESHOLD,
                onToggleSwitchPosition = {},
                shouldBeLocked = { true }
            )
        }
    }
}

@Preview
@Composable
private fun LockSwitchLockedPressedRecordingPreview() {
    // box is here to account for the offset lock switch
    Box(modifier = Modifier.width(150.dp), contentAlignment = Alignment.CenterEnd) {
        CaptureButtonRing(captureButtonSize = DEFAULT_CAPTURE_BUTTON_SIZE, color = Color.White) {
            LockSwitchCaptureButtonNucleus(
                captureButtonSize = DEFAULT_CAPTURE_BUTTON_SIZE,
                captureButtonUiState = CaptureButtonUiState.Enabled.RecordingTimelapse,
                switchWidth = (DEFAULT_CAPTURE_BUTTON_SIZE * LOCK_SWITCH_WIDTH_SCALE).dp,
                switchPosition = 1f,
                onToggleSwitchPosition = {},
                shouldBeLocked = { true }
            )
        }
    }
}
