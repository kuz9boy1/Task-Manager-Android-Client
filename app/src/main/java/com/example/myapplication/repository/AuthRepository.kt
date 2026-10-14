package com.example.myapplication.repository

import android.content.Context
import com.example.myapplication.api.ApiClient
import com.example.myapplication.data.NetworkUtils
import com.example.myapplication.dto.AuthResponseDto
import com.example.myapplication.dto.LoginRequestDto
import com.example.myapplication.dto.RegisterRequestDto
import com.example.myapplication.local.AppDatabase
import com.example.myapplication.local.user.UserEntity

class AuthRepository(private val context: Context) {

    private val userDao = AppDatabase.get(context).userDao()

    suspend fun register(login: String, password: String): AuthResponseDto {
        if (userDao.findByLogin(login) != null) {
            return AuthResponseDto(false, "Логин '$login' уже занят")
        }

        if (NetworkUtils.isOnline(context)) {
            return try {
                val response = ApiClient.authApi.register(
                    com.example.myapplication.dto.RegisterRequestDto(login, password)
                )
                val body = response.body()
                if (body?.success == true) {
                    userDao.insert(
                        UserEntity(
                            login = login,
                            password = password,
                            serverId = body.userId,
                            isSynced = true
                        )
                    )
                    body
                } else {
                    body ?: AuthResponseDto(false, "Ошибка регистрации")
                }
            } catch (e: Exception) {
                saveLocalUser(login, password)
                AuthResponseDto(
                    success = true,
                    message = "Регистрация сохранена локально (нет сети)",
                    login = login
                )
            }
        } else {
            saveLocalUser(login, password)
            return AuthResponseDto(
                success = true,
                message = "Регистрация выполнена локально",
                login = login
            )
        }
    }

    suspend fun login(login: String, password: String): AuthResponseDto {
        if (NetworkUtils.isOnline(context)) {
            return try {
                val response = ApiClient.authApi.login(
                    com.example.myapplication.dto.LoginRequestDto(login, password)
                )
                val body = response.body()
                if (body?.success == true) {
                    userDao.insert(
                        UserEntity(
                            login = login,
                            password = password,
                            serverId = body.userId,
                            isSynced = true,
                            lastLogin = System.currentTimeMillis()
                        )
                    )
                    body
                } else {
                    body ?: AuthResponseDto(false, "Ошибка входа")
                }
            } catch (e: Exception) {
                localLogin(login, password)
            }
        } else {
            return localLogin(login, password)
        }
    }

    private suspend fun localLogin(login: String, password: String): AuthResponseDto {
        val user = userDao.findByLogin(login)
            ?: return AuthResponseDto(false, "Нет сети. Пользователь не найден локально")

        if (user.password != password) {
            return AuthResponseDto(false, "Нет сети. Неверный пароль")
        }

        userDao.insert(user.copy(lastLogin = System.currentTimeMillis()))

        return AuthResponseDto(
            success = true,
            message = "Вход offline",
            userId = user.serverId,
            login = user.login
        )
    }

    private suspend fun saveLocalUser(login: String, password: String) {
        userDao.insert(
            UserEntity(
                login = login,
                password = password,
                serverId = null,
                isSynced = false
            )
        )
    }

    suspend fun syncPendingUsers() {
        if (!NetworkUtils.isOnline(context)) return

        val unsynced = userDao.getUnsynced()
        for (user in unsynced) {
            try {
                val response = ApiClient.authApi.register(
                    com.example.myapplication.dto.RegisterRequestDto(user.login, user.password)
                )
                val body = response.body()
                if (body?.success == true && body.userId != null) {
                    userDao.markSynced(user.login, body.userId)
                }
            } catch (e: Exception) {
            }
        }
    }
}