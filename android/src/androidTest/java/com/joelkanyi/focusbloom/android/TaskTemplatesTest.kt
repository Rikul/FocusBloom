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

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskTemplatesTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        clearDatabase()
    }

    @Test
    fun verifyTemplateDropdownAndSaveButtonExist() {
        composeTestRule.waitForIdle()
        
        handleNotificationPermission(device)
        completeOnboarding(composeTestRule)
        completeUsername(composeTestRule)

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

        // Verify we're on Add Task screen
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithText("Add Task")
            .assertIsDisplayed()

        // Verify Template dropdown exists
        composeTestRule
            .onNodeWithText("Template")
            .assertIsDisplayed()

        // Verify Save as Template button exists
        composeTestRule
            .onNodeWithText("Save as Template")
            .assertIsDisplayed()
    }

    @Test
    fun verifyTaskTemplatesSectionVisible() {
        composeTestRule.waitForIdle()

        // Handle notification permission (Android 13+)
        handleNotificationPermission(device)

        // Complete onboarding flow
        completeOnboarding(composeTestRule)
        completeUsername(composeTestRule)

        // Now we should be on the home screen, find the FAB
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

        // Click on Settings bottom navigation item
        composeTestRule
            .onNodeWithTag("Settings")
            .assertIsDisplayed()
            .performClick()

        // Verify Task Templates section is visible
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithText("Task Templates")
            .assertIsDisplayed()
    }

    @Test
    fun createTestTemplate() {
        composeTestRule.waitForIdle()

        handleNotificationPermission(device)
        completeOnboarding(composeTestRule)
        completeUsername(composeTestRule)

        // Now we should be on the home screen, find the FAB
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

        // Click Save as Template
        composeTestRule
            .onNodeWithText("Save as Template")
            .performClick()

        // Verify Dialog appears
        composeTestRule.waitForIdle()
        composeTestRule
            .onNode(hasText("Save as Template") and hasClickAction().not()) // Dialog Title
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Template Name")
            .assertIsDisplayed()

        composeTestRule
            .onNode(hasText("Save") and hasTestTag("save_task_button").not())
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Cancel")
            .assertIsDisplayed()

        // Enter template name
        val templateName = "My Test Template"
        composeTestRule
            .onNodeWithText("Enter name")
            .performTextInput(templateName)

        // Click Save
        composeTestRule
            .onNode(hasText("Save") and hasTestTag("save_task_button").not())
            .performClick()

        composeTestRule.waitForIdle()

         composeTestRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeTestRule
                    .onNodeWithText("Template saved!")
                    .assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.waitForIdle()

        // back to home screen
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
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

    @Test
    fun useTemplatesAndSaveTask() {
        composeTestRule.waitForIdle()

        handleNotificationPermission(device)
        completeOnboarding(composeTestRule)
        completeUsername(composeTestRule)

        createTemplate("Template 1", "Desc 1", "Work")

        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag("add_task_fab").assertExists()
                true
            } catch (e: AssertionError) { false }
        }

        createTemplate("Template 2", "Desc 2", "Study")

        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag("add_task_fab").assertExists()
                true
            } catch (e: AssertionError) { false }
        }
        composeTestRule.onNodeWithTag("add_task_fab").performClick()

        // Select Template 1
        composeTestRule.onNodeWithText("Select Template").performClick()
        composeTestRule.onNodeWithText("Template 1").performClick()

        // Verify fields for Template 1
        composeTestRule.onNodeWithTag("task_name_input").assertTextEquals("Template 1")
        composeTestRule.onNodeWithText("Desc 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Work").assertIsDisplayed()

        // Select Template 2
        // Open dropdown (it now shows "Template 1")
        composeTestRule.onNode(
            hasText("Template 1") and hasTestTag("task_name_input").not()
        ).performClick()
        
        composeTestRule.onNodeWithText("Template 2").performClick()

        // Verify fields for Template 2
        composeTestRule.onNodeWithTag("task_name_input").assertTextEquals("Template 2")
        composeTestRule.onNodeWithText("Desc 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Study").assertIsDisplayed()

        // Save Task
        composeTestRule.onNodeWithTag("save_task_button").performClick()

        // Verify Task Saved
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeTestRule.onNodeWithText("Task added!").assertExists()
                true
            } catch (e: AssertionError) { false }
        }
        
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag("Home").assertExists()
                true
            } catch (e: AssertionError) { false }
        }
    }

    @Test
    fun verifyTemplatesInSettings() {
        composeTestRule.waitForIdle()

        handleNotificationPermission(device)
        completeOnboarding(composeTestRule)
        completeUsername(composeTestRule)

        // Create Template 1
        createTemplate("Template 1", "Desc 1", "Work")

        // Create Template 2
        createTemplate("Template 2", "Desc 2", "Study")

        //  Go to Settings
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag("Settings").assertExists()
                true
            } catch (e: AssertionError) { false }
        }
        composeTestRule.onNodeWithTag("Settings").performClick()

        //  Expand Task Templates section
        composeTestRule.onNodeWithText("Task Templates").performClick()

        // Verify templates are displayed
        composeTestRule.onNodeWithText("Template 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Template 2").assertIsDisplayed()
    }

    @Test
    fun verifyTemplateEditDeleteActionsInSettings() {
        composeTestRule.waitForIdle()

        handleNotificationPermission(device)
        completeOnboarding(composeTestRule)
        completeUsername(composeTestRule)

        // Create Template 1
        createTemplate("Template 1", "Desc 1", "Work")

        // Go to Settings
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag("Settings").assertExists()
                true
            } catch (e: AssertionError) { false }
        }
        composeTestRule.onNodeWithTag("Settings").performClick()

        // Expand Task Templates section
        composeTestRule.onNodeWithText("Task Templates").performClick()

        // Verify Edit and Delete icons exist for the template
        composeTestRule.onNodeWithText("Template 1").assertIsDisplayed()
        
        composeTestRule
            .onNode(hasContentDescription("Edit"))
            .assertIsDisplayed()

        composeTestRule
            .onNode(hasContentDescription("Delete"))
            .assertIsDisplayed()
    }

    @Test
    fun verifyDeleteTemplateConfirmationDialog() {
        composeTestRule.waitForIdle()

        handleNotificationPermission(device)
        completeOnboarding(composeTestRule)
        completeUsername(composeTestRule)

        // Create Template 1
        createTemplate("Template To Delete", "Desc", "Work")

        // Go to Settings
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag("Settings").assertExists()
                true
            } catch (e: AssertionError) { false }
        }
        composeTestRule.onNodeWithTag("Settings").performClick()

        // Expand Task Templates section
        composeTestRule.onNodeWithText("Task Templates").performClick()

        // Click Delete icon
        composeTestRule
            .onNode(hasContentDescription("Delete"))
            .performClick()

        // Verify Confirmation Dialog
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Delete Template").assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure you want to delete this template?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun verifyEditTemplateNavigationAndData() {
        composeTestRule.waitForIdle()

        handleNotificationPermission(device)
        completeOnboarding(composeTestRule)
        completeUsername(composeTestRule)

        // Create Template
        val templateName = "Test For Edit Template"
        val taskName = "Task To Edit"
        val description = "Description To Edit"
        val type = "Study"
        
        createTemplate(templateName, description, type)

        // Go to Settings
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag("Settings").assertExists()
                true
            } catch (e: AssertionError) { false }
        }
        composeTestRule.onNodeWithTag("Settings").performClick()

        // Expand Task Templates section
        composeTestRule.onNodeWithText("Task Templates").performClick()

        // Click Edit icon
        composeTestRule
            .onNode(hasContentDescription("Edit"))
            .performClick()

        // Verify Edit Screen
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Edit Template").assertIsDisplayed()

        // Verify Data
        composeTestRule.onAllNodesWithText(templateName).assertCountEquals(2)
        
        // Description
        composeTestRule.onNodeWithText(description).assertIsDisplayed()
        
        // Type
        composeTestRule.onNodeWithText(type).assertIsDisplayed()
    }

    @Test
    fun verifyEditTemplatePersistsChanges() {
        composeTestRule.waitForIdle()

        handleNotificationPermission(device)
        completeOnboarding(composeTestRule)
        completeUsername(composeTestRule)

        // Create Template
        val originalName = "Original Template"
        createTemplate(originalName, "Original Desc", "Work")

        // Go to Settings
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag("Settings").assertExists()
                true
            } catch (e: AssertionError) { false }
        }
        composeTestRule.onNodeWithTag("Settings").performClick()

        // Expand Task Templates section
        composeTestRule.onNodeWithText("Task Templates").performClick()

        // Click Edit icon
        composeTestRule
            .onNode(hasContentDescription("Edit"))
            .performClick()

        // Modify Data
        composeTestRule.waitForIdle()
        
        val updatedName = "Updated Template"
        
        // Clear the first text field (Template Name)
        composeTestRule
            .onAllNodes(hasSetTextAction())[0]
            .performTextClearance()
            
        // Enter new name
        composeTestRule
            .onAllNodes(hasSetTextAction())[0]
            .performTextInput(updatedName)
            
        // Save
        composeTestRule.onNodeWithText("Save Template").performClick()
        
        // Verify in Settings
        composeTestRule.waitForIdle()
        // It should return to Settings screen
        composeTestRule.onNode(hasText("Settings") and hasTestTag("Settings").not()).assertIsDisplayed() // Title in TopBar
        
        // Check if updated name is present
        composeTestRule.onNodeWithText(updatedName).assertIsDisplayed()
        
        // Check if original name is gone
        composeTestRule.onNodeWithText(originalName).assertDoesNotExist()
    }

    @Test
    fun verifyCancelSaveTemplateDialog() {
        composeTestRule.waitForIdle()

        handleNotificationPermission(device)
        completeOnboarding(composeTestRule)
        completeUsername(composeTestRule)

        // Go to Add Task
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag("add_task_fab").assertExists()
                true
            } catch (e: AssertionError) { false }
        }
        composeTestRule.onNodeWithTag("add_task_fab").performClick()

        // Click Save as Template
        composeTestRule.onNodeWithText("Save as Template").performClick()

        //  Verify Dialog appears
        composeTestRule.waitForIdle()
        composeTestRule
            .onNode(hasText("Save as Template") and hasClickAction().not())
            .assertIsDisplayed()

        // Click Cancel
        composeTestRule.onNodeWithText("Cancel").performClick()

        // Verify Dialog is dismissed
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Template Name").assertDoesNotExist()

        // Verify we are still on Add Task screen
        composeTestRule.onNodeWithText("Add Task").assertIsDisplayed()
    }

    @Test
    fun verifyCancelDeleteTemplateDialog() {
        composeTestRule.waitForIdle()

        handleNotificationPermission(device)
        completeOnboarding(composeTestRule)
        completeUsername(composeTestRule)

        // Create Template
        val templateName = "Template To Keep"
        createTemplate(templateName, "Desc", "Work")

        // Go to Settings
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag("Settings").assertExists()
                true
            } catch (e: AssertionError) { false }
        }
        composeTestRule.onNodeWithTag("Settings").performClick()

        // Expand Task Templates section
        composeTestRule.onNodeWithText("Task Templates").performClick()

        // Click Delete icon
        composeTestRule
            .onNode(hasContentDescription("Delete"))
            .performClick()

        // Click Cancel
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Cancel").performClick()

        // Verify Dialog is dismissed and Template still exists
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Delete Template").assertDoesNotExist()
        composeTestRule.onNodeWithText(templateName).assertIsDisplayed()
    }

    private fun createTemplate(name: String, description: String, type: String) {
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag("add_task_fab").assertExists()
                true
            } catch (e: AssertionError) { false }
        }
        composeTestRule.onNodeWithTag("add_task_fab").performClick()

        composeTestRule.onNodeWithTag("task_name_input").performTextInput(name)
        composeTestRule.onNodeWithText("Enter Description").performTextInput(description)
        
        // Select Type (Default is "Other")
        composeTestRule.onNodeWithText("Other").performClick()
        composeTestRule.onNodeWithText(type).performClick()
        
        // Save as Template
        composeTestRule.onNodeWithText("Save as Template").performClick()
        composeTestRule.onNodeWithText("Enter name").performTextInput(name)
        composeTestRule.onNode(hasText("Save") and hasTestTag("save_task_button").not()).performClick()
        
        // Wait for return to home
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeTestRule.onNodeWithTag("Home").assertExists()
                true
            } catch (e: AssertionError) { false }
        }
    }
}
