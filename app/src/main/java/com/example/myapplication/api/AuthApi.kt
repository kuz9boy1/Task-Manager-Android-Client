package com.example.myapplication.api


import com.example.myapplication.dto.AuthResponseDto
import com.example.myapplication.dto.LoginRequestDto
import com.example.myapplication.dto.RegisterRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<AuthResponseDto>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<AuthResponseDto>
}