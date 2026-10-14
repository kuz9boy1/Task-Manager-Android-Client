package com.example.myapplication.dto

data class TaskRequestDto(
    val userId: Long,
    val title: String,
    val description: String,
    val dueDate: String?,
    val status: String = "TODO"
)