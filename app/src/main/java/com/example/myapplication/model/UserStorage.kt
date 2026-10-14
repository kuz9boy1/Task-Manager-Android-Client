package com.example.myapplication.model

import android.content.Context

object UserStorage {
    private const val PREFS_NAME = "users_prefs"

    fun register(context: Context, login: String, password: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.contains(login)) {
            return false
        }
        prefs.edit().putString(login, password).apply()
        return true
    }

    fun isRegistered(context: Context, login: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.contains(login)
    }

    fun checkCredentials(context: Context, login: String, password: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedPassword = prefs.getString(login, null) ?: return false
        return savedPassword == password
    }
}