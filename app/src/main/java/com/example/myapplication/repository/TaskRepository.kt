package com.example.myapplication.repository

import android.content.Context
import com.example.myapplication.api.ApiClient
import com.example.myapplication.data.NetworkUtils
import com.example.myapplication.dto.TaskRequestDto
import com.example.myapplication.dto.TaskResponseDto
import com.example.myapplication.local.AppDatabase
import com.example.myapplication.local.TaskEntity

class TaskRepository(private val context: Context) {

    private val dao = AppDatabase.get(context).taskDao()


    suspend fun getTasks(login: String, serverUserId: Long?): List<TaskResponseDto> {
        if (NetworkUtils.isOnline(context) && serverUserId != null) {
            try {
                val response = ApiClient.taskApi.getTasks(serverUserId)
                if (response.isSuccessful && response.body() != null) {
                    val serverTasks = response.body()!!

                    val unsynced = dao.getUnsynced().filter { it.userLogin == login }

                    dao.clearSyncedTasks(login)

                    serverTasks.forEach { task ->
                        dao.insert(task.toEntity(login, synced = true))
                    }

                    return serverTasks + unsynced.map { it.toDto() }
                }
            } catch (e: Exception) {
            }
        }
        return dao.getTasks(login).map { it.toDto() }
    }

    suspend fun createTask(
        login: String,
        serverUserId: Long?,
        request: TaskRequestDto
    ): TaskResponseDto? {
        if (NetworkUtils.isOnline(context) && serverUserId != null) {
            try {
                val response = ApiClient.taskApi.createTask(request)
                if (response.isSuccessful && response.body() != null) {
                    val serverTask = response.body()!!
                    dao.insert(serverTask.toEntity(login, synced = true))
                    return serverTask
                }
            } catch (e: Exception) {
            }
        }

        val localId = dao.insert(
            TaskEntity(
                serverId = null,
                userLogin = login,
                title = request.title,
                description = request.description,
                dueDate = request.dueDate,
                status = request.status,
                isSynced = false,
                isDeleted = false
            )
        )
        return TaskResponseDto(
            id = localId,
            title = request.title,
            description = request.description,
            dueDate = request.dueDate,
            status = request.status,
            userId = serverUserId
        )
    }


    suspend fun updateTask(
        login: String,
        serverUserId: Long?,
        taskId: Long,
        request: TaskRequestDto
    ): TaskResponseDto? {
        val localEntity = dao.getByLocalId(taskId)
            ?: dao.getByServerId(taskId)
            ?: return null

        if (NetworkUtils.isOnline(context) && serverUserId != null && localEntity.serverId != null) {
            try {
                val response = ApiClient.taskApi.updateTask(localEntity.serverId, request)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    dao.update(
                        localEntity.copy(
                            title = body.title,
                            description = body.description,
                            dueDate = body.dueDate,
                            status = body.status,
                            isSynced = true
                        )
                    )
                    return body
                }
            } catch (e: Exception) {
            }
        }

        dao.update(
            localEntity.copy(
                title = request.title,
                description = request.description,
                dueDate = request.dueDate,
                status = request.status,
                isSynced = false
            )
        )
        return localEntity.copy(
            title = request.title,
            description = request.description,
            dueDate = request.dueDate,
            status = request.status
        ).toDto()
    }

    suspend fun deleteTask(
        login: String,
        serverUserId: Long?,
        taskId: Long
    ): Boolean {
        val localEntity = dao.getByLocalId(taskId)
            ?: dao.getByServerId(taskId)
            ?: return false

        if (NetworkUtils.isOnline(context) && serverUserId != null && localEntity.serverId != null) {
            try {
                val response = ApiClient.taskApi.deleteTask(localEntity.serverId, serverUserId)
                if (response.isSuccessful) {
                    dao.deleteByServerId(localEntity.serverId)
                    return true
                }
            } catch (e: Exception) {
            }
        }

        dao.update(localEntity.copy(isDeleted = true, isSynced = false))
        return true
    }


    suspend fun syncPendingTasks(login: String, serverUserId: Long?) {
        if (!NetworkUtils.isOnline(context) || serverUserId == null) return

        val unsynced = dao.getUnsynced().filter { it.userLogin == login }

        for (task in unsynced) {
            try {
                when {
                    task.isDeleted && task.serverId != null -> {
                        ApiClient.taskApi.deleteTask(task.serverId, serverUserId)
                        dao.deleteByServerId(task.serverId)
                    }

                    task.serverId == null -> {
                        val response = ApiClient.taskApi.createTask(
                            TaskRequestDto(
                                userId = serverUserId,
                                title = task.title,
                                description = task.description,
                                dueDate = task.dueDate,
                                status = task.status
                            )
                        )
                        if (response.isSuccessful && response.body() != null) {
                            val serverTask = response.body()!!
                            dao.update(
                                task.copy(
                                    serverId = serverTask.id,
                                    isSynced = true
                                )
                            )
                        }
                    }
                    else -> {
                        val response = ApiClient.taskApi.updateTask(
                            task.serverId,
                            TaskRequestDto(
                                userId = serverUserId,
                                title = task.title,
                                description = task.description,
                                dueDate = task.dueDate,
                                status = task.status
                            )
                        )
                        if (response.isSuccessful) {
                            dao.update(task.copy(isSynced = true))
                        }
                    }
                }
            } catch (e: Exception) {
            }
        }
    }


    private fun TaskResponseDto.toEntity(login: String, synced: Boolean) = TaskEntity(
        serverId = this.id,
        userLogin = login,
        title = this.title,
        description = this.description,
        dueDate = this.dueDate,
        status = this.status,
        isSynced = synced,
        isDeleted = false
    )

    private fun TaskEntity.toDto() = TaskResponseDto(
        id = this.serverId ?: this.localId,
        title = this.title,
        description = this.description,
        dueDate = this.dueDate,
        status = this.status,
        userId = null
    )
}