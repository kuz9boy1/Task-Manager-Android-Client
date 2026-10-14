package com.example.myapplication.dto

data class TaskResponseDto(
    val id: Long?,
    val title: String,
    val description: String,
    val dueDate: String?,
    val status: String,
    val userId: Long?
)