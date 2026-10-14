package com.example.myapplication.local.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users_local")
data class UserEntity(
    @PrimaryKey
    val login: String,                // логин — первичный ключ

    val serverId: Long? = null,       // id на сервере (null = ещё не синхронизирован)
    val password: String,             // ⚠️ для учебного проекта — plaintext
    val isSynced: Boolean = false,    // был ли зарегистрирован на сервере
    val lastLogin: Long = 0L          // время последнего входа
)