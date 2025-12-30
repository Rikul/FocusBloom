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
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for adding a task in FocusBloom app.
 *
 * This test verifies the complete flow of:
 * 1. Handling notification permission dialog (Android 13+)
 * 2. Completing the 3-page onboarding flow
 * 3. Setting up username
 * 4. Clicking the Add Task FAB button
 * 5. Entering task details
 * 6. Saving the task
 *
 * Test runs on Android device/emulator.
 */
@RunWith(AndroidJUnit4::class)
class AddTaskTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    /**
     * Test case: Create a simple task using the UI
     *
     * Steps:
     * 1. Handle notification permission dialog (if Android 13+)
     * 2. Complete 3-page onboarding (Next -> Next -> Get Started)
     * 3. Enter username and continue
     * 4. Find and click the + (Add Task) FAB button
     * 5. Verify we're on the Add Task screen
     * 6. Enter task name
     * 7. Click Save button
     * 8. Verify task was created (returns to home screen)
     */
    @Test
    fun clickAddButtonAndCreateTask() {
        // Step 1: Handle notification permission (Android 13+)
        handleNotificationPermission()

        // Step 2: Complete onboarding flow
        completeOnboarding()
        completeUsername()

        // Step 3: Now we should be on the home screen, find the FAB
        composeTestRule.waitForIdle()

        // Wait for FAB to appear (with longer timeout)
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            try {
                composeTestRule
                    .onNodeWithTag("add_task_fab")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        // Click the Add Task FAB
        composeTestRule
            .onNodeWithTag("add_task_fab")
            .assertIsDisplayed()
            .performClick()

        // Step 4: Verify we're on Add Task screen
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithText("Add Task")
            .assertIsDisplayed()

        // Step 5: Enter task name
        val taskName = "Test Task from Instrumented Test"

        // Click on the text field first to focus it
        composeTestRule
            .onNodeWithTag("task_name_input")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        // Now type the text
        composeTestRule
            .onNodeWithTag("task_name_input")
            .performTextInput(taskName)

        // Step 6: Click Save button
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag("save_task_button")
            .assertIsDisplayed()
            .performClick()

        // Step 7: Verify task was created
        composeTestRule.waitForIdle()

        // Wait for either the snackbar or home screen
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            try {
                // Check if we see "Task added!" snackbar
                composeTestRule
                    .onNodeWithText("Task added!", substring = true, ignoreCase = true)
                    .assertExists()
                true
            } catch (e: AssertionError) {
                // Or check if we're back on home screen by looking for bottom navigation
                try {
                    composeTestRule
                        .onNodeWithTag("Home")
                        .assertExists()
                    true
                } catch (e2: AssertionError) {
                    false
                }
            }
        }
    }

    /**
     * Test case: Verify Add Task button exists after onboarding
     */
    @Test
    fun addTaskButtonExistsAfterOnboarding() {
        handleNotificationPermission()
        completeOnboarding()
        completeUsername()

        composeTestRule.waitForIdle()

        // Wait for FAB
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            try {
                composeTestRule
                    .onNodeWithTag("add_task_fab")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule
            .onNodeWithTag("add_task_fab")
            .assertIsDisplayed()
    }

    /**
     * Test case: Verify onboarding flow completes successfully
     */
    @Test
    fun onboardingFlowCompletes() {
        handleNotificationPermission()
        completeOnboarding()
        completeUsername()

        composeTestRule.waitForIdle()

        // Verify we reached the home screen (FAB should be visible)
        composeTestRule.waitUntil(timeoutMillis = 15_000) {
            try {
                composeTestRule
                    .onNodeWithTag("add_task_fab")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }

    // ========== Helper Methods ==========

    /**
     * Handles the Android notification permission dialog (Android 13+)
     */
    private fun handleNotificationPermission() {
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
     * 4. Enter username and click "Continue"
     */
    private fun completeOnboarding() {
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

    private fun completeUsername() {
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
}
