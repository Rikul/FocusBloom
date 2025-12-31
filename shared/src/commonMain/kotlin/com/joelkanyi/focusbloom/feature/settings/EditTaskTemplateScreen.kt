/*
 * Copyright 2025 Joel Kanyi.
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
package com.joelkanyi.focusbloom.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.joelkanyi.focusbloom.core.domain.model.TextFieldState
import com.joelkanyi.focusbloom.core.domain.model.taskTypes
import com.joelkanyi.focusbloom.core.presentation.component.*
import com.joelkanyi.focusbloom.core.utils.UiEvents
import com.joelkanyi.focusbloom.core.utils.formattedTimeBasedOnTimeFormat
import com.joelkanyi.focusbloom.core.utils.koinViewModel
import com.joelkanyi.focusbloom.platform.StatusBarColors
import com.joelkanyi.focusbloom.feature.addtask.TimerInputDialog
import focusbloom.shared.generated.resources.Res
import focusbloom.shared.generated.resources.start_time
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalTime
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskTemplateScreen(
    navController: NavController,
    templateId: Int,
    viewModel: EditTaskTemplateViewModel = koinViewModel(),
) {
    StatusBarColors(
        statusBarColor = MaterialTheme.colorScheme.background,
        navBarColor = MaterialTheme.colorScheme.background,
    )
    val taskName = viewModel.taskName.value
    val templateName = viewModel.templateName.value
    val taskDescription = viewModel.taskDescription.value
    val selectedTaskType = viewModel.selectedOption.collectAsState().value
    val focusSessions = viewModel.focusSessions.collectAsState().value
    val startTime = viewModel.startTime.collectAsState().value
    val showStartTimeInputDialog = viewModel.showStartTimeInputDialog.collectAsState().value
    val hourFormat = viewModel.hourFormat.collectAsState().value ?: 24

    val startTimeState = rememberTimePickerState(
        initialHour = startTime.hour,
        initialMinute = startTime.minute,
        is24Hour = hourFormat == 24,
    )

    LaunchedEffect(key1 = true) {
        viewModel.getTemplate(templateId)
        withContext(Dispatchers.Main.immediate) {
            viewModel.eventsFlow.collect { event ->
                if (event is UiEvents.NavigateBack) {
                    navController.popBackStack()
                }
            }
        }
    }

    if (showStartTimeInputDialog) {
        TimerInputDialog(
            title = "Start Time",
            state = startTimeState,
            onDismiss = {
                viewModel.setShowStartTimeInputDialog(false)
            },
            onConfirmStartTime = {
                viewModel.setStartTime(it)
                viewModel.recalculateEndTime()
                viewModel.setShowStartTimeInputDialog(false)
            },
        )
    }

    Scaffold(
        topBar = {
             BloomTopAppBar(
                 hasBackNavigation = true,
                 navigationIcon = {
                     androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) {
                         Icon(androidx.compose.material.icons.Icons.Filled.ArrowBack, contentDescription = "Back")
                     }
                 },
                 title = {
                     Text(text = "Edit Template")
                 }
             )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
             item {
                 BloomInputTextField(
                     modifier = Modifier.fillMaxWidth(),
                     value = TextFieldState(templateName),
                     onValueChange = { viewModel.setTemplateName(it) },
                     label = {
                         Text("Template Name", style = MaterialTheme.typography.titleSmall)
                     }
                 )
             }
             item {
                 BloomInputTextField(
                     modifier = Modifier.fillMaxWidth(),
                     value = TextFieldState(taskName),
                     onValueChange = { viewModel.setTaskName(it) },
                     label = {
                         Text("Task Name", style = MaterialTheme.typography.titleSmall)
                     },
                     placeholder = { Text("Task Name") }
                 )
             }
             item {
                 BloomInputTextField(
                     modifier = Modifier.fillMaxWidth(),
                     value = TextFieldState(taskDescription),
                     onValueChange = { viewModel.setTaskDescription(it) },
                     label = {
                         Text("Description", style = MaterialTheme.typography.titleSmall)
                     },
                     maxLines = 5,
                 )
             }
             item {
                BloomDropDown(
                    label = { Text("Task Type", style = MaterialTheme.typography.titleSmall) },
                    modifier = Modifier.fillMaxWidth(),
                    options = taskTypes,
                    selectedOption = TextFieldState(selectedTaskType.name),
                    onOptionSelected = { viewModel.setSelectedOption(it) },
                )
             }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                TimeComponent(
                    time = startTime,
                    hourFormat = hourFormat,
                    title = "Start Time",
                    icon = Res.drawable.start_time,
                    iconColor = MaterialTheme.colorScheme.primary,
                    iconSize = 24,
                    onClick = { viewModel.setShowStartTimeInputDialog(true) },
                )
            }

            item {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Focus Sessions",
                    style = MaterialTheme.typography.titleMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                    ),
                )
            }

            item {
                BloomIncrementer(
                    modifier = Modifier.fillMaxWidth(),
                    onClickRemove = {
                        viewModel.decrementFocusSessions()
                    },
                    onClickAdd = {
                        viewModel.incrementFocusSessions()
                    },
                    currentValue = focusSessions,
                )
            }

             item {
                 Spacer(modifier = Modifier.height(16.dp))
                 BloomButton(
                     modifier = Modifier.fillMaxWidth().height(56.dp),
                     onClick = { viewModel.saveTemplate() }
                 ) {
                     Text("Save Template")
                 }
             }
        }
    }
}

@Composable
private fun TimeComponent(
    title: String,
    icon: DrawableResource,
    iconColor: Color,
    iconSize: Int = 32,
    time: LocalTime,
    hourFormat: Int,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.clickable {
            onClick()
        },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            ),
        )
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = time.formattedTimeBasedOnTimeFormat(hourFormat),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 16.sp,
                ),
            )

            Icon(
                modifier = Modifier
                    .size(iconSize.dp),
                painter = painterResource(icon),
                contentDescription = title,
                tint = iconColor,
            )
        }
    }
}
