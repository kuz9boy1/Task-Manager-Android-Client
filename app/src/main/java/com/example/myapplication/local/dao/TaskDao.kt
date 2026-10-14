package com.example.myapplication.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.local.TaskEntity

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks_local WHERE userLogin = :login AND isDeleted = 0")
    suspend fun getTasks(login: String): List<TaskEntity>

    @Query("SELECT * FROM tasks_local WHERE localId = :localId LIMIT 1")
    suspend fun getByLocalId(localId: Long): TaskEntity?

    @Query("SELECT * FROM tasks_local WHERE serverId = :serverId LIMIT 1")
    suspend fun getByServerId(serverId: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Query("DELETE FROM tasks_local WHERE userLogin = :login AND isSynced = 1")
    suspend fun clearSyncedTasks(login: String)

    @Query("SELECT * FROM tasks_local WHERE isSynced = 0")
    suspend fun getUnsynced(): List<TaskEntity>

    @Query("DELETE FROM tasks_local WHERE serverId = :serverId")
    suspend fun deleteByServerId(serverId: Long)
}