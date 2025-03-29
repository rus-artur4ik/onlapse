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

package com.google.jetpackcamera.settings.model

enum class TimelapseFrequencyConfig(val shotsPerDay: Int) {
    ONE_PER_DAY(1),
    TWO_PER_DAY(2),
    FOUR_PER_DAY(4),
    SIX_PER_DAY(6),
    TWELVE_PER_DAY(12),
    TWENTY_FOUR_PER_DAY(24),
    THIRTY_PER_DAY(30),
    SIXTY_PER_DAY(60),
    ONE_HUNDRED_TWENTY_PER_DAY(120),
}