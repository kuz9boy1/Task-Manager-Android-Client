package com.example.myapplication.local.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users_local")
data class UserEntity(
    @PrimaryKey
    val login: String,

    val serverId: Long? = null,
    val password: String,
    val isSynced: Boolean = false,
    val lastLogin: Long = 0L
)