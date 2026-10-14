package com.example.myapplication.dto

data class AuthResponseDto(
    val success: Boolean,
    val message: String,
    val userId: Long? = null,
    val login: String? = null
)