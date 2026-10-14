package com.example.myapplication.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks_local")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,

    val serverId: Long? = null,
    val userLogin: String,
    val title: String,
    val description: String,
    val dueDate: String?,
    val status: String,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)