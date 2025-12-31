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
package com.joelkanyi.focusbloom.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import com.joelkanyi.focusbloom.core.data.mapper.toTaskTemplate
import com.joelkanyi.focusbloom.core.data.mapper.toTaskTemplateEntity
import com.joelkanyi.focusbloom.core.data.utils.mapToList
import com.joelkanyi.focusbloom.core.domain.model.TaskTemplate
import com.joelkanyi.focusbloom.core.domain.repository.TaskTemplateRepository
import com.joelkanyi.focusbloom.database.BloomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskTemplateRepositoryImpl(
    bloomDatabase: BloomDatabase,
) : TaskTemplateRepository {
    private val dbQuery = bloomDatabase.taskTemplateQueries

    override fun getAllTaskTemplates(): Flow<List<TaskTemplate>> {
        return dbQuery.getAllTaskTemplates()
            .asFlow()
            .mapToList()
            .map { entities ->
                entities.map { it.toTaskTemplate() }
            }
    }

    override suspend fun getTaskTemplate(id: Int): TaskTemplate? {
        return dbQuery.getTaskTemplateById(id)
            .executeAsOneOrNull()
            ?.toTaskTemplate()
    }

    override suspend fun addTaskTemplate(taskTemplate: TaskTemplate) {
        taskTemplate.toTaskTemplateEntity().let {
            dbQuery.insertTaskTemplate(
                name = it.name,
                taskName = it.taskName,
                taskDescription = it.taskDescription,
                type = it.type,
                startTime = it.startTime,
                color = it.color,
                focusSessions = it.focusSessions
            )
        }
    }

    override suspend fun updateTaskTemplate(taskTemplate: TaskTemplate) {
        taskTemplate.toTaskTemplateEntity().let {
            dbQuery.updateTaskTemplate(
                name = it.name,
                taskName = it.taskName,
                taskDescription = it.taskDescription,
                type = it.type,
                startTime = it.startTime,
                color = it.color,
                focusSessions = it.focusSessions,
                id = it.id
            )
        }
    }

    override suspend fun deleteTaskTemplate(id: Int) {
        dbQuery.deleteTaskTemplate(id)
    }

    override suspend fun deleteAllTaskTemplates() {
        dbQuery.deleteAllTaskTemplates()
    }
}
