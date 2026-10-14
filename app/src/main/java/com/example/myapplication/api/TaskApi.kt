package com.example.myapplication.api

import com.example.myapplication.dto.TaskRequestDto
import com.example.myapplication.dto.TaskResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskApi {

    @GET("api/tasks")
    suspend fun getTasks(@Query("userId") userId: Long): Response<List<TaskResponseDto>>

    @POST("api/tasks")
    suspend fun createTask(@Body request: TaskRequestDto): Response<TaskResponseDto>

    @PUT("api/tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: Long,
        @Body request: TaskRequestDto
    ): Response<TaskResponseDto>

    @DELETE("api/tasks/{id}")
    suspend fun deleteTask(
        @Path("id") id: Long,
        @Query("userId") userId: Long
    ): Response<Unit>
}