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
package com.joelkanyi.focusbloom.core.domain.repository

import com.joelkanyi.focusbloom.core.domain.model.TaskTemplate
import kotlinx.coroutines.flow.Flow

interface TaskTemplateRepository {
    fun getAllTaskTemplates(): Flow<List<TaskTemplate>>
    suspend fun getTaskTemplate(id: Int): TaskTemplate?
    suspend fun addTaskTemplate(taskTemplate: TaskTemplate)
    suspend fun updateTaskTemplate(taskTemplate: TaskTemplate)
    suspend fun deleteTaskTemplate(id: Int)
    suspend fun deleteAllTaskTemplates()
}
