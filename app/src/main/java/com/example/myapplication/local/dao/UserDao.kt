package com.example.myapplication.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.local.user.UserEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM users_local WHERE login = :login LIMIT 1")
    suspend fun findByLogin(login: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("SELECT * FROM users_local WHERE isSynced = 0")
    suspend fun getUnsynced(): List<UserEntity>

    @Query("UPDATE users_local SET isSynced = 1, serverId = :serverId WHERE login = :login")
    suspend fun markSynced(login: String, serverId: Long)

    @Delete
    suspend fun delete(user: UserEntity)
}