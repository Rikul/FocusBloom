/*
 * Copyright 2023 Joel Kanyi.
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
package com.joelkanyi.focusbloom.android

import android.os.Build
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.joelkanyi.focusbloom.core.domain.repository.tasks.TasksRepository
import com.joelkanyi.focusbloom.core.domain.repository.TaskTemplateRepository
import org.koin.core.context.GlobalContext
import kotlinx.coroutines.runBlocking

/**
 * Test helper functions for FocusBloom instrumented tests.
 * These utilities handle common test scenarios like onboarding and permissions.
 */

/**
 * Clears the database (Tasks and Task Templates) to ensure a clean state for tests.
 */
fun clearDatabase() {
    val koin = GlobalContext.get()
    val tasksRepository = koin.get<TasksRepository>()
    val taskTemplateRepository = koin.get<TaskTemplateRepository>()
    
    runBlocking {
        tasksRepository.deleteAllTasks()
        taskTemplateRepository.deleteAllTaskTemplates()
    }
}

/**
 * Handles the Android notification permission dialog (Android 13+)
 */
fun handleNotificationPermission(device: UiDevice) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Wait for permission dialog to appear
        val permissionDialog = device.wait(
            Until.findObject(By.text("Allow")),
            2000
        )

        if (permissionDialog != null) {
            // Click "Allow" button
            try {
                val allowButton = device.findObject(
                    UiSelector()
                        .text("Allow")
                        .className("android.widget.Button")
                )
                if (allowButton.exists()) {
                    allowButton.click()
                    Thread.sleep(1000) // Wait for dialog to dismiss
                }
            } catch (e: Exception) {
                // If clicking by text fails, try the more generic approach
                try {
                    device.findObject(By.text("Allow"))?.click()
                    Thread.sleep(1000)
                } catch (e2: Exception) {
                    // Permission dialog might have been dismissed or not shown
                    // Continue with test
                }
            }
        }
    }
}

/**
 * Completes the full onboarding flow:
 * 1. Click "Next" on page 1
 * 2. Click "Next" on page 2
 * 3. Click "Get Started" on page 3
 *
 * If onboarding is already completed, this function returns early.
 */
fun completeOnboarding(composeTestRule: ComposeTestRule) {
    composeTestRule.waitForIdle()

    // Page 1: Click "Next" (or return if onboarding already done)
    try {
        composeTestRule.waitUntil(timeoutMillis = 1_000) {
            try {
                composeTestRule
                    .onNodeWithTag("onboarding_next_button")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    } catch (e: ComposeTimeoutException) {
        // Onboarding button not found - onboarding already complete
        return
    }

    composeTestRule
        .onNodeWithTag("onboarding_next_button")
        .performClick()

    composeTestRule.waitForIdle()

    // Page 2: Click "Next" again
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
        try {
            composeTestRule
                .onNodeWithTag("onboarding_next_button")
                .assertExists()
            true
        } catch (e: AssertionError) {
            false
        }
    }

    composeTestRule
        .onNodeWithTag("onboarding_next_button")
        .performClick()

    composeTestRule.waitForIdle()

    // Page 3: Click "Get Started"
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
        try {
            composeTestRule
                .onNodeWithTag("onboarding_get_started_button")
                .assertExists()
            true
        } catch (e: AssertionError) {
            false
        }
    }

    composeTestRule
        .onNodeWithTag("onboarding_get_started_button")
        .performClick()
}

/**
 * Completes the username entry screen.
 * Enters "TestUser" as the username and clicks Continue.
 *
 * If already past the username screen, this function returns early.
 */
fun completeUsername(composeTestRule: ComposeTestRule) {
    composeTestRule.waitForIdle()

    // Username screen: Enter username (or return if already on home screen)
    try {
        composeTestRule.waitUntil(timeoutMillis = 1_000) {
            try {
                composeTestRule
                    .onNodeWithTag("username_input")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    } catch (e: ComposeTimeoutException) {
        // Username screen not found - already on home screen
        return
    }

    composeTestRule
        .onNodeWithTag("username_input")
        .performTextInput("TestUser")

    composeTestRule.waitForIdle()

    // Click Continue button (it appears after username is entered)
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
        try {
            composeTestRule
                .onNodeWithTag("username_continue_button")
                .assertExists()
            true
        } catch (e: AssertionError) {
            false
        }
    }

    composeTestRule
        .onNodeWithTag("username_continue_button")
        .performClick()

    // Wait for navigation to complete
    Thread.sleep(2000)
}
