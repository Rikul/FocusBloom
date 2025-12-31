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

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joelkanyi.focusbloom.core.domain.model.Task
import com.joelkanyi.focusbloom.core.domain.model.TaskTemplate
import com.joelkanyi.focusbloom.core.domain.model.TaskType
import com.joelkanyi.focusbloom.core.domain.model.taskTypes
import com.joelkanyi.focusbloom.core.domain.repository.TaskTemplateRepository
import com.joelkanyi.focusbloom.core.domain.repository.settings.SettingsRepository
import com.joelkanyi.focusbloom.core.utils.UiEvents
import com.joelkanyi.focusbloom.core.utils.calculateFromFocusSessions
import com.joelkanyi.focusbloom.core.utils.today
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime

class EditTaskTemplateViewModel(
    settingsRepository: SettingsRepository,
    private val taskTemplateRepository: TaskTemplateRepository,
) : ViewModel() {
    private val _eventsFlow = Channel<UiEvents>(Channel.UNLIMITED)
    val eventsFlow = _eventsFlow.receiveAsFlow()

    val sessionTime = settingsRepository.getSessionTime()
        .map { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )
    val shortBreakTime = settingsRepository.getShortBreakTime()
        .map { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )
    val longBreakTime = settingsRepository.getLongBreakTime()
        .map { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )
    val hourFormat = settingsRepository.getHourFormat()
        .map { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    private val _focusSessions = MutableStateFlow(1)
    val focusSessions = _focusSessions.asStateFlow()

    private val _taskName = mutableStateOf("")
    val taskName: State<String> = _taskName

    private val _templateName = mutableStateOf("")
    val templateName: State<String> = _templateName

    private val _taskDescription = mutableStateOf("")
    val taskDescription: State<String> = _taskDescription

    private val _selectedOption = MutableStateFlow(taskTypes.last())
    val selectedOption = _selectedOption.asStateFlow()

    private val _startTime = MutableStateFlow(today().time)
    val startTime = _startTime.asStateFlow()

    private val _endTime = MutableStateFlow(today().time)
    val endTime = _endTime.asStateFlow()

    private val _showStartTimeInputDialog = MutableStateFlow(false)
    val showStartTimeInputDialog = _showStartTimeInputDialog.asStateFlow()

    private val _templateId = MutableStateFlow<Int?>(null)

    fun setTemplateName(name: String) {
        _templateName.value = name
    }

    fun setTaskName(name: String) {
        _taskName.value = name
    }

    fun setTaskDescription(description: String) {
        _taskDescription.value = description
    }

    fun setSelectedOption(option: TaskType) {
        _selectedOption.value = option
    }

    fun setStartTime(time: LocalTime) {
        _startTime.value = time
    }

    fun setEndTime(time: LocalTime) {
        _endTime.value = time
    }

    fun setShowStartTimeInputDialog(show: Boolean) {
        _showStartTimeInputDialog.value = show
    }

    fun incrementFocusSessions() {
        _focusSessions.value++
        recalculateEndTime()
    }

    fun decrementFocusSessions() {
        if (_focusSessions.value > 0) {
            _focusSessions.value--
            recalculateEndTime()
        }
    }

    private fun setFocusSessions(sessions: Int) {
        _focusSessions.value = sessions
        recalculateEndTime()
    }

    fun recalculateEndTime() {
        setEndTime(
            calculateFromFocusSessions(
                focusSessions = focusSessions.value,
                sessionTime = sessionTime.value ?: 25,
                shortBreakTime = shortBreakTime.value ?: 5,
                longBreakTime = longBreakTime.value ?: 15,
                currentLocalDateTime = LocalDateTime(
                    year = today().year,
                    month = today().month,
                    dayOfMonth = today().dayOfMonth,
                    hour = startTime.value.hour,
                    minute = startTime.value.minute,
                ),
            ),
        )
    }

    fun getTemplate(id: Int) {
        viewModelScope.launch {
            _templateId.value = id
            val template = taskTemplateRepository.getTaskTemplate(id)
            template?.let {
                setTemplateName(it.name)
                setTaskName(it.taskName)
                setTaskDescription(it.taskDescription ?: "")
                setSelectedOption(taskTypes.find { t -> t.name == it.type } ?: taskTypes.last())
                // Ensure focus sessions is set before calculating time, but verify start time first
                _startTime.value = it.startTime
                setFocusSessions(it.focusSessions)
                // setFocusSessions triggered recalculateEndTime
            }
        }
    }

    fun saveTemplate() {
        viewModelScope.launch {
            val id = _templateId.value
            if (id != null) {
                taskTemplateRepository.updateTaskTemplate(
                     TaskTemplate(
                         id = id,
                         name = templateName.value,
                         taskName = taskName.value,
                         taskDescription = taskDescription.value,
                         type = selectedOption.value.name,
                         startTime = startTime.value,
                         color = selectedOption.value.color,
                         focusSessions = focusSessions.value
                     )
                )
                _eventsFlow.trySend(UiEvents.ShowSnackbar("Template updated"))
                _eventsFlow.trySend(UiEvents.NavigateBack)
            }
        }
    }
}
